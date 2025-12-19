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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
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
                participantName = ParticipantNameUtils.buildTeamName(team);
            }

            default -> throw new UnsupportedOperationException("Unsupported match type: " + type);
        }

        leagueRepository.save(league);

        return "Účastník '" + participantName + "' bol úspešne odstránený z ligy a "
                + (affectedMatches.isEmpty() ? "nebol zapojený do žiadneho zápasu." :
                "a všetky jeho zapasy (" + affectedMatches.size() + ") boli zmazané.");
    }

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
                participantName = ParticipantNameUtils.buildTeamName(team);
            }
            default -> throw new UnsupportedOperationException("Unsupported match type: " + type);
        }

        league.getDroppedParticipantsIds().add(participantId);
        leagueRepository.save(league);

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

        // Hráči
        List<ParticipantDTO> players = league.getPlayers().stream()
                .map(player -> {
                    String name = ParticipantNameUtils.buildPlayerName(player);
                    int progress = playerStatsService.playerProgress(league.getId(), player.getId());
                    return new ParticipantDTO(player.getId(), name, player.isActive(), progress);
                })
                .toList();
        dto.setPlayers(players);

        // Tímy
        List<ParticipantDTO> teams = league.getTeams().stream()
                .map(team -> {
                    String name = ParticipantNameUtils.buildTeamName(team);
                    int progress = teamStatsService.teamProgress(league.getId(), team.getId());
                    return new ParticipantDTO(team.getId(), name, team.isActive(), progress);
                })
                .toList();
        dto.setTeams(teams);

        // Víťaz
        if (league.getStatus() == LeagueStatus.FINISHED) {
            dto.setWinner(determineWinnerName(league));
        }

        // Progress
        dto.setLeagueProgress(calculateProgress(league.getId()));

        return dto;
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

    private int calculateProgress(String leagueId) {
        int played = matchService.getPlayedMatchesCount(leagueId);
        int total = matchService.getTotalMatchesCount(leagueId);
        return total == 0 ? 0 : (int) ((double) played / total * 100);
    }
}
