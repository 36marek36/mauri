package com.example.mauri.mapper;

import com.example.mauri.model.League;
import com.example.mauri.model.Season;
import com.example.mauri.model.VolleyLeague;
import com.example.mauri.model.dto.request.PlayerShortDTO;
import com.example.mauri.model.dto.request.SeasonTennisLeagueSummaryDTO;
import com.example.mauri.model.dto.request.TeamShortDTO;
import com.example.mauri.model.dto.request.VolleyTeamShortDTO;
import com.example.mauri.model.dto.response.*;
import com.example.mauri.service.LeagueService;
import com.example.mauri.util.ParticipantNameUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@AllArgsConstructor
public class SeasonMapper {

    private final LeagueService leagueService;
    private final VolleyLeagueMapper volleyLeagueMapper;

    public SeasonResponseDTO mapSeasonToDTO(Season season, boolean includeLeagues) {
        List<LeagueResponseDTO> leagueDTOs = new ArrayList<>();
        List<VolleyLeagueResponseDTO> volleyLeagueDTOs = new ArrayList<>();
        long totalPlayers = 0;
        long totalTeams = 0;

        Set<String> participantIds = new HashSet<>();

        if (includeLeagues && season.getLeagues() != null) {

            List<League> sortedLeagues = new ArrayList<>(season.getLeagues());
            sortedLeagues.sort(Comparator
                    .comparingInt(this::priority)
                    .thenComparing(League::getName));

            for (League league : sortedLeagues) {
                LeagueResponseDTO leagueDTO = leagueService.getFullLeagueDTO(league);
                leagueDTOs.add(leagueDTO);

                totalPlayers += leagueDTO.getPlayers() != null ? leagueDTO.getPlayers().size() : 0;
                totalTeams += leagueDTO.getTeams() != null ? leagueDTO.getTeams().size() : 0;
                // single hráči
                league.getPlayers()
                        .forEach(player -> participantIds.add(player.getId()));
                // doubles hráči
                league.getTeams()
                        .forEach(team -> {
                            if (team.getPlayer1() != null) {
                                participantIds.add(team.getPlayer1().getId());
                            }
                            if (team.getPlayer2() != null) {
                                participantIds.add(team.getPlayer2().getId());
                            }
                        });
            }
        }

        if (includeLeagues && season.getVolleyLeagues() != null) {
            volleyLeagueDTOs = season.getVolleyLeagues().stream()
                    .map(volleyLeagueMapper::mapToResponseDTO)
                    .toList();
        }
        return SeasonResponseDTO.builder()
                .id(season.getId())
                .year(season.getYear())
                .status(season.getStatus())
                .leagues(leagueDTOs)
                .volleyLeagues(volleyLeagueDTOs)
                .totalPlayers(totalPlayers)
                .totalTeams(totalTeams)
                .totalParticipants(participantIds.size())
                .createdAt(season.getCreatedAt())
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .build();
    }

    public SeasonManagementResponseDTO mapSeasonToManagementDTO(Season season) {

        List<LeagueManagementResponseDTO> leagues = season.getLeagues() == null ? List.of() : season.getLeagues()
                .stream()
                .map(this::mapLeagueToManagementDTO)
                .toList();

        List<VolleyLeagueManagementResponseDTO> volleyLeagues = season.getVolleyLeagues() == null ? List.of() : season.getVolleyLeagues()
                .stream()
                .map(this::mapVolleyLeagueToManagementDTO)
                .toList();

        return SeasonManagementResponseDTO.builder()
                .id(season.getId())
                .year(season.getYear())
                .status(season.getStatus())
                .leagues(leagues)
                .volleyLeagues(volleyLeagues)
                .build();
    }

    public TennisSeasonDetailResponseDTO mapToTennisSeasonDetailDTO(
            Season season,
            long totalLeagues,
            long totalPlayers,
            long totalTeams,
            long totalParticipants,
            long totalMatches,
            long totalFinishedMatches,
            long totalScratchedMatches,
            long totalCancelledMatches,
            long totalCompletedMatches,
            List<SeasonTennisLeagueSummaryDTO> leagueSummaries
    ) {
        return TennisSeasonDetailResponseDTO.builder()
                .id(season.getId())
                .year(season.getYear())
                .status(season.getStatus())
                .totalLeagues(totalLeagues)
                .totalPlayers(totalPlayers)
                .totalTeams(totalTeams)
                .totalParticipants(totalParticipants)
                .totalMatches(totalMatches)
                .totalFinishedMatches(totalFinishedMatches)
                .totalScratchedMatches(totalScratchedMatches)
                .totalCancelledMatches(totalCancelledMatches)
                .totalCompletedMatches(totalCompletedMatches)
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .leagues(leagueSummaries)
                .build();
    }

    public TennisSeasonListResponseDTO mapToSeasonTennisLeaguesDTO(
            Season season,
            long totalLeagues,
            long totalPlayers,
            long totalTeams,
            long totalParticipants,
            long totalMatches
    ) {
        return TennisSeasonListResponseDTO.builder()
                .id(season.getId())
                .year(season.getYear())
                .status(season.getStatus())
                .totalLeagues(totalLeagues)
                .totalPlayers(totalPlayers)
                .totalTeams(totalTeams)
                .totalParticipants(totalParticipants)
                .totalMatches(totalMatches)
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .build();
    }

    public VolleySeasonListResponseDTO mapToVolleySeasonListDTO(
            Season season,
            long totalLeagues,
            long totalTeams,
            long totalMatches,
            long totalFinishedMatches,
            long totalScratchedMatches,
            long totalCancelledMatches,
            long totalCompletedMatches
    ) {
        return VolleySeasonListResponseDTO.builder()
                .id(season.getId())
                .year(season.getYear())
                .status(season.getStatus())
                .totalLeagues(totalLeagues)
                .totalTeams(totalTeams)
                .totalMatches(totalMatches)
                .totalFinishedMatches(totalFinishedMatches)
                .totalScratchedMatches(totalScratchedMatches)
                .totalCancelledMatches(totalCancelledMatches)
                .totalCompletedMatches(totalCompletedMatches)
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .build();
    }

    private LeagueManagementResponseDTO mapLeagueToManagementDTO(League league) {

        List<PlayerShortDTO> players = league.getPlayers() == null ? List.of() : league.getPlayers()
                .stream()
                .map(player -> new PlayerShortDTO(
                        player.getId(),
                        ParticipantNameUtils.buildPlayerName(player)
                ))
                .toList();

        List<TeamShortDTO> teams = league.getTeams() == null ? List.of() : league.getTeams()
                .stream()
                .map(team -> new TeamShortDTO(
                        team.getId(),
                        ParticipantNameUtils.buildTeamName(team)
                ))
                .toList();

        return LeagueManagementResponseDTO.builder()
                .id(league.getId())
                .name(league.getName())
                .leagueType(league.getLeagueType())
                .players(players)
                .teams(teams)
                .build();
    }

    private VolleyLeagueManagementResponseDTO mapVolleyLeagueToManagementDTO(VolleyLeague league) {
        List<VolleyTeamShortDTO> teams = league.getTeams() == null ? List.of() : league.getTeams()
                .stream()
                .map(team -> new VolleyTeamShortDTO(
                        team.getId(),
                        team.getName()
                ))
                .toList();

        return VolleyLeagueManagementResponseDTO.builder()
                .id(league.getId())
                .name(league.getName())
                .teams(teams)
                .build();
    }


    private int priority(League league) {
        String name = league.getName().toLowerCase();

        if (name.contains("ženy")) return 0;
        if (name.contains("extraliga")) return 1;
        if (name.matches(".*mu([žz])i.*[1-3].*")) return 2;

        return 3;
    }
}
