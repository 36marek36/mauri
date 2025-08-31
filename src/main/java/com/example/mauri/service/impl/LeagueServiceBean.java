package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.exception.ResourceNotFoundException;
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
    private final PlayerStatsService playerStatsService;
    private final TeamStatsService teamStatsService;


    @Override
    public List<LeagueResponseDTO> getAllLeagues() {
        List<League> leagues = leagueRepository.findAll();

//        return leagues.stream()
//                .map(this::mapLeagueToDTO)
//                .toList();

        List<LeagueResponseDTO> leagueDTOs = new ArrayList<>();
        for (League league : leagues) {
            LeagueResponseDTO dto = mapLeagueToDTO(league);
            leagueDTOs.add(dto);
        }

        return leagueDTOs;
    }

    @Override
    public LeagueResponseDTO getLeagueById(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));

        return mapLeagueToDTO(league);
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
        return mapLeagueToDTO(league);
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
                    MatchResult result = new MatchResult();
                    result.setScratchedId(participantId);
                    matchService.addResult(match.getId(), result);
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
                    MatchResult result = new MatchResult();
                    result.setScratchedId(participantId);
                    matchService.addResult(match.getId(), result);
                }

                league.getTeams().remove(team);
                participantName = ParticipantNameUtils.buildTeamName(team);
            }

            default -> throw new UnsupportedOperationException("Unsupported match type: " + type);
        }

        leagueRepository.save(league);

        return "Účastník '" + participantName + "' bol úspešne odstránený z ligy a "
                + (affectedMatches.isEmpty() ? "nebol zapojený do žiadneho zápasu." :
                "bol vyradený z " + affectedMatches.size() + " zápasov.");
    }

    @Override
    public List<LeagueResponseDTO> getLeaguesForPlayer(String playerId) {
        List<League> leagues = leagueRepository.findLeaguesByPlayerId(playerId);
        List<LeagueResponseDTO> result = new ArrayList<>();

        for (League league : leagues) {
            LeagueResponseDTO dto = mapLeagueToDTO(league);
            result.add(dto);
        }

        return result;
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
            if (match.getResult() != null &&
                    match.getResult().getScore1() != null &&
                    match.getResult().getScore2() != null &&
                    match.getResult().getWinnerId() != null) {
                match.setStatus(MatchStatus.FINISHED);
            } else {
                match.setStatus(MatchStatus.CANCELLED);
            }
        }
        matchRepository.saveAll(matches);
    }
    private String getLeagueWinnerName(String leagueId, MatchType leagueType) {
        if (leagueType == MatchType.SINGLES) {
            List<PlayerStatsDTO> stats = playerStatsService.getAllStatsForLeague(leagueId);
            if (stats.isEmpty() || stats.stream().allMatch(s -> s.getMatches() == 0)) {
                return "Liga nemá víťaza, žiadny zápas nebol odohraný.";
            }
            return stats.getFirst().getPlayerName();
        } else if (leagueType == MatchType.DOUBLES) {
            List<TeamStatsDTO> stats = teamStatsService.getAllStatsForLeague(leagueId);
            if (stats.isEmpty() || stats.stream().allMatch(s -> s.getMatches() == 0)) {
                return "Liga nemá víťaza, žiadny zápas nebol odohraný.";
            }
            return stats.getFirst().getTeamName();
        }
        return null;
    }

    private int progress(String leagueId) {
        int played = matchService.getPlayedMatchesCount(leagueId);
        int total = matchService.getTotalMatchesCount(leagueId);

        if (total == 0) return 0;

        return (int) ((double) played / total * 100);
    }

    @Override
    public LeagueResponseDTO mapLeagueToDTO(League league) {
        LeagueResponseDTO dto = new LeagueResponseDTO();

        dto.setLeagueId(league.getId());
        dto.setLeagueName(league.getName());
        dto.setSeasonYear(league.getSeason() != null ? league.getSeason().getYear() : null);
        dto.setLeagueType(league.getLeagueType());
        dto.setLeagueStatus(league.getStatus());
        dto.setWinner(null);

        // Vytvorenie zoznamu mien hráčov (ak sú)
        List<ParticipantDTO> players = new ArrayList<>();
        if (league.getPlayers() != null) {
            for (Player p : league.getPlayers()) {
                String playerName = ParticipantNameUtils.buildPlayerName(p);
                players.add(new ParticipantDTO(p.getId(), playerName));
            }
        }
        dto.setPlayers(players);

        // Vytvorenie zoznamu mien tímov
        List<ParticipantDTO> teams = new ArrayList<>();
        if (league.getTeams() != null) {
            for (Team t : league.getTeams()) {
                String teamName = ParticipantNameUtils.buildTeamName(t);
                teams.add(new ParticipantDTO(t.getId(), teamName));
            }
        }
        dto.setTeams(teams);

        // Ak je liga skončená, skús získať víťaza
        if (league.getStatus() == LeagueStatus.FINISHED) {
            try {
                String winnerName = getLeagueWinnerName(league.getId(), league.getLeagueType());
                dto.setWinner(winnerName);
            } catch (Exception e) {
                log.warn("Získanie víťaza ligy {} zlyhalo: {}", league.getId(), e.getMessage());
            }
        }

        // Progress
        int progress = progress(league.getId());
        dto.setLeagueProgress(progress);

        return dto;
    }
}
