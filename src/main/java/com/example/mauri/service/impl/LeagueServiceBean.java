package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.LeagueMapper;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.create.CreateLeagueDTO;
import com.example.mauri.model.dto.request.ParticipantDTO;
import com.example.mauri.model.dto.response.LeagueResponseDTO;
import com.example.mauri.model.dto.response.PlayerStatsDTO;
import com.example.mauri.model.dto.response.TeamStatsDTO;
import com.example.mauri.model.dto.update.ReplaceTeamDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.LeagueService;
import com.example.mauri.service.MatchService;
import com.example.mauri.service.PlayerStatsService;
import com.example.mauri.service.TeamStatsService;
import com.example.mauri.util.ParticipantNameUtils;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeagueServiceBean implements LeagueService {

    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;
    private final MatchService matchService;
    private final LeagueMapper leagueMapper;
    private final PlayerStatsService playerStatsService;
    private final TeamStatsService teamStatsService;


    @Override
    public List<LeagueResponseDTO> getAllLeagues() {
        List<League> leagues = leagueRepository.findAll();

        return leagues.stream()
                .map(this::getFullLeagueDTO)
                .toList();

    }

    @Override
    public LeagueResponseDTO getLeagueById(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("{} viewed league details: {} in season {}", username, league.getName(), league.getSeason().getYear());

        return getFullLeagueDTO(league);
    }

    @Override
    public LeagueResponseDTO createLeague(CreateLeagueDTO createLeagueDTO) {
        Season season = null;

        if (StringUtils.hasText(createLeagueDTO.getSeasonId())) {
            season = seasonRepository.findById(createLeagueDTO.getSeasonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Season not found with id: " + createLeagueDTO.getSeasonId()));
        }

        League league = League.builder()
                .id(UUID.randomUUID().toString())
                .leagueType(createLeagueDTO.getLeagueType())
                .name(createLeagueDTO.getName())
                .season(season)
                .build();

        league = leagueRepository.save(league);

        log.info("League {} created successfully", league.getName());

        return leagueMapper.mapLeagueToDTO(league);
    }

    @Override
    @Transactional
    public void deleteLeagueById(@NonNull String id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No league found with id: " + id));

        List<Match> matches = matchRepository.findByLeagueId(league.getId());
        matchRepository.deleteAll(matches);

        league.getPlayers().clear();
        league.getTeams().clear();

        leagueRepository.delete(league);
    }

    @Override
    @Transactional
    public String addParticipantsToLeague(String leagueId, List<String> participantIds) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("No league found with id: " + leagueId));

        MatchType type = league.getLeagueType();

        switch (type) {
            case SINGLES -> {
                List<Player> players = playerRepository.findAllById(participantIds);
                for (Player player : players) {
                    if (!league.getPlayers().contains(player)) {
                        league.getPlayers().add(player);
                    }
                }
            }
            case DOUBLES -> {
                List<Team> teams = teamRepository.findAllById(participantIds);
                for (Team team : teams) {
                    if (!league.getTeams().contains(team)) {
                        league.getTeams().add(team);
                    }
                }
            }
        }

        leagueRepository.save(league);
        return "Účastníci boli úspešne pridaní do ligy.";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public String removeParticipantFromLeague(String leagueId, String participantId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("No league found with id: " + leagueId));

        MatchType type = league.getLeagueType();
        List<Match> affectedMatches;
        String participantName;

        switch (type) {
            case SINGLES -> {
                Player player = playerRepository.findById(participantId)
                        .orElseThrow(() -> new ResourceNotFoundException("No player found with id: " + participantId));

                if (!league.getPlayers().contains(player)) {
                    throw new IllegalStateException("Player is not part of the league.");
                }

                affectedMatches = matchRepository.findByLeagueIdAndPlayer(leagueId, participantId);
                for (Match match : affectedMatches) {
                    matchService.deleteMatch(match.getId());
                }
                league.getPlayers().remove(player);
                participantName = ParticipantNameUtils.buildPlayerName(player);
            }

            case DOUBLES -> {
                Team team = teamRepository.findById(participantId)
                        .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + participantId));

                if (!league.getTeams().contains(team)) {
                    throw new IllegalStateException("Team is not part of the league.");
                }

                affectedMatches = matchRepository.findByLeagueIdAndTeam(leagueId, participantId);
                for (Match match : affectedMatches) {
                    matchService.deleteMatch(match.getId());
                }
                league.getTeams().remove(team);
                participantName = ParticipantNameUtils.buildTeamShortName(team);
            }

            default -> throw new UnsupportedOperationException("Unsupported match type: " + type);
        }

        leagueRepository.save(league);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("{} removed '{}' from league '{}'", username, participantName, league.getName());

        return "Účastník '" + participantName + "' bol úspešne odstránený z ligy a "
                + (affectedMatches.isEmpty() ? "nebol zapojený do žiadneho zápasu." :
                "a všetky jeho zapasy (" + affectedMatches.size() + ") boli zmazané.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    public String dropParticipantFromLeague(String leagueId, String participantId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("No league found with id: " + leagueId));

        if (league.getDroppedParticipantsIds() == null) {
            league.setDroppedParticipantsIds(new ArrayList<>());
        }

        if (league.getDroppedParticipantsIds().contains(participantId)) {
            return "Účastník už je označený ako odstúpený z ligy.";
        }

        if (league.getStatus() == LeagueStatus.CREATED) {
            return "Je zbytočné hráča odhlasovať z neaktívnej ligy. Možeš ho z ligy radšej odstrániť.";
        }

        MatchType type = league.getLeagueType();
        List<Match> affectedMatches;
        String participantName;

        switch (type) {
            case SINGLES -> {
                Player player = playerRepository.findById(participantId)
                        .orElseThrow(() -> new ResourceNotFoundException("No player found with id: " + participantId));

                if (!league.getPlayers().contains(player)) {
                    throw new IllegalStateException("Player is not part of the league.");
                }

                affectedMatches = matchRepository.findByLeagueIdAndPlayer(leagueId, participantId);
                for (Match match : affectedMatches) {
//                    MatchResult result = new MatchResult();
//                    result.setScratchedId(participantId);
//                    matchService.addResult(match.getId(), result);

                    match.setStatus(MatchStatus.CANCELLED);

//                    matchService.deleteMatch(match.getId());
                }
                participantName = ParticipantNameUtils.buildPlayerName(player);
            }

            case DOUBLES -> {
                Team team = teamRepository.findById(participantId)
                        .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + participantId));

                if (!league.getTeams().contains(team)) {
                    throw new IllegalStateException("Team is not part of the league.");
                }

                affectedMatches = matchRepository.findByLeagueIdAndTeam(leagueId, participantId);
                for (Match match : affectedMatches) {
                    match.setStatus(MatchStatus.CANCELLED);
//                    matchService.deleteMatch(match.getId());
                }
                participantName = ParticipantNameUtils.buildTeamShortName(team);
            }
            default -> throw new UnsupportedOperationException("Unsupported match type: " + type);
        }

        league.getDroppedParticipantsIds().add(participantId);
        leagueRepository.save(league);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("{} dropped '{}' from league '{}'", username, participantName, league.getName());

        return "Účastník '" + participantName + "' bol úspešne odhlásený z ligy a všetky svoje zapasy (" + affectedMatches.size() + ") boli zrušené.";
    }

    @Override
    public void finishLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("No league found with id: " + leagueId));

        if (league.getStatus().equals(LeagueStatus.FINISHED)) {
            throw new IllegalStateException("League already finished");
        }

        league.setStatus(LeagueStatus.FINISHED);
        leagueRepository.save(league);

        List<Match> matches = matchRepository.findByLeagueId(leagueId);

        for (Match match : matches) {
            if (match.getStatus() == MatchStatus.CREATED) {
                match.setStatus(MatchStatus.CANCELLED);
            }
        }

        matchRepository.saveAll(matches);
    }

    @Override
    public LeagueResponseDTO getFullLeagueDTO(League league) {

        LeagueResponseDTO dto = leagueMapper.mapLeagueToDTO(league);
        // ALL matches
        List<Match> allMatches =
                matchRepository.findByLeagueId(league.getId());

        // PLAYED matches
        List<Match> evaluatedMatches = allMatches.stream()
                .filter(m -> m.getStatus().isPlayed())
                .toList();

        // Progress mapy
        Map<String, Integer> playerProgressMap =
                playerStatsService.calculatePlayerProgress(
                        allMatches,
                        league.getPlayers());

        Map<String, Integer> teamProgressMap =
                teamStatsService.calculateTeamProgress(
                        allMatches,
                        league.getTeams());


        // Hráči
        List<ParticipantDTO> players = league.getPlayers().stream()
                .map(player -> {
                    String name = ParticipantNameUtils.buildPlayerName(player);
                    int progress = playerProgressMap.getOrDefault(player.getId(), 0);
                    return new ParticipantDTO(player.getId(), name, player.isActive(), progress);
                })
                .toList();
        dto.setPlayers(players);

        // Tímy
        List<ParticipantDTO> teams = league.getTeams().stream()
                .map(team -> {
                    String name = ParticipantNameUtils.buildTeamShortName(team);
                    int progress = teamProgressMap.getOrDefault(team.getId(), 0);
                    return new ParticipantDTO(team.getId(), name, team.isActive(), progress);
                })
                .toList();
        dto.setTeams(teams);

        // Víťaz
        if (league.getStatus() == LeagueStatus.FINISHED) {
            dto.setWinner(determineWinnerName(league));
        }

        // Progress
        dto.setLeagueProgress(
                calculateProgress(evaluatedMatches, allMatches));

        return dto;
    }

    @Override
    @Transactional
    public void replaceTeamInLeague(String leagueId, ReplaceTeamDTO replaceTeamDTO) {
        Team oldTeam = teamRepository.findById(replaceTeamDTO.getOldTeamId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No team found with id: " + replaceTeamDTO.getOldTeamId()));

        Team newTeam = teamRepository.findById(replaceTeamDTO.getNewTeamId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No team found with id: " + replaceTeamDTO.getNewTeamId()));

        if (oldTeam.getId().equals(newTeam.getId())) {
            throw new IllegalArgumentException("Old and new team cannot be the same.");
        }

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No league found with id: " + leagueId));

        boolean oldTeamInLeague = league.getTeams().stream()
                .anyMatch(team -> team.getId().equals(oldTeam.getId()));

        if (!oldTeamInLeague) {
            throw new IllegalArgumentException("Old team is not part of this league.");
        }

        boolean newTeamInLeague = league.getTeams().stream()
                .anyMatch(team -> team.getId().equals(newTeam.getId()));

        if (newTeamInLeague) {
            throw new IllegalArgumentException("New team is already part of this league.");
        }

        List<Match> matches = matchRepository.findByLeagueIdAndTeam(
                leagueId,
                oldTeam.getId());

        for (Match match : matches) {

            if (match.getHomeTeam().getId().equals(oldTeam.getId())) {
                match.setHomeTeam(newTeam);
            }

            if (match.getAwayTeam().getId().equals(oldTeam.getId())) {
                match.setAwayTeam(newTeam);
            }
            if (match.getResult() != null) {

                if (oldTeam.getId().equals(match.getResult().getWinnerId())) {
                    match.getResult().setWinnerId(newTeam.getId());
                }

                if (oldTeam.getId().equals(match.getResult().getScratchedId())) {
                    match.getResult().setScratchedId(newTeam.getId());
                }
            }
        }

        List<Team> teams = league.getTeams();

        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getId().equals(oldTeam.getId())) {
                teams.set(i, newTeam);
                break;
            }
        }
    }

    private String determineWinnerName(League league) {
        if (league.getLeagueType() == MatchType.SINGLES) {
            List<PlayerStatsDTO> stats = playerStatsService.getAllStatsForLeague(league.getId());
            if (stats.isEmpty() || stats.stream().allMatch(s -> s.getMatches() == 0)) {
                return "Liga nemá víťaza, žiadny zápas nebol odohraný.";
            }
            return stats.getFirst().getPlayerName();
        } else {
            List<TeamStatsDTO> stats = teamStatsService.getAllStatsForLeague(league.getId());
            if (stats.isEmpty() || stats.stream().allMatch(s -> s.getMatches() == 0)) {
                return "Liga nemá víťaza, žiadny zápas nebol odohraný.";
            }
            return stats.getFirst().getTeamName();
        }
    }

    private int calculateProgress(
            List<Match> evaluatedMatches,
            List<Match> allMatches) {

        long played = evaluatedMatches.size();
        long total = allMatches.size();

        return total == 0
                ? 0
                : (int) ((played * 100) / total);
    }
}
