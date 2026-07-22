package com.example.mauri.mapper;

import com.example.mauri.model.League;
import com.example.mauri.model.Season;
import com.example.mauri.model.dto.response.LeagueResponseDTO;
import com.example.mauri.model.dto.response.SeasonResponseDTO;
import com.example.mauri.model.dto.response.VolleyLeagueResponseDTO;
import com.example.mauri.service.LeagueService;
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


    private int priority(League league) {
        String name = league.getName().toLowerCase();

        if (name.contains("ženy")) return 0;
        if (name.contains("extraliga")) return 1;
        if (name.matches(".*mu([žz])i.*[1-3].*")) return 2;

        return 3;
    }
}
