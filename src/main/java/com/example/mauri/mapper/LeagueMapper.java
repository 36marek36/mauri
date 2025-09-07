package com.example.mauri.mapper;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.model.League;
import com.example.mauri.model.Player;
import com.example.mauri.model.Team;
import com.example.mauri.model.dto.request.ParticipantDTO;
import com.example.mauri.model.dto.response.LeagueResponseDTO;
import com.example.mauri.model.dto.response.PlayerStatsDTO;
import com.example.mauri.model.dto.response.TeamStatsDTO;
import com.example.mauri.service.MatchService;
import com.example.mauri.service.PlayerStatsService;
import com.example.mauri.service.TeamStatsService;
import com.example.mauri.util.ParticipantNameUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class LeagueMapper {

    private final MatchService matchService;
    private final TeamStatsService teamStatsService;
    private final PlayerStatsService playerStatsService;

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
                players.add(new ParticipantDTO(p.getId(), playerName, p.isActive()));
            }
        }
        dto.setPlayers(players);

        // Vytvorenie zoznamu mien tímov
        List<ParticipantDTO> teams = new ArrayList<>();
        if (league.getTeams() != null) {
            for (Team t : league.getTeams()) {
                String teamName = ParticipantNameUtils.buildTeamName(t);
                teams.add(new ParticipantDTO(t.getId(), teamName, t.isActive()));
            }
        }
        dto.setTeams(teams);

        dto.setDroppedParticipantsIds(league.getDroppedParticipantsIds());

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
}
