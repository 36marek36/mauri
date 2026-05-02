package com.example.mauri.mapper;

import com.example.mauri.model.League;
import com.example.mauri.model.Season;
import com.example.mauri.model.dto.response.LeagueResponseDTO;
import com.example.mauri.model.dto.response.SeasonResponseDTO;
import com.example.mauri.service.LeagueService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@AllArgsConstructor
public class SeasonMapper {

    private final LeagueService leagueService;

    public SeasonResponseDTO mapSeasonToDTO(Season season, boolean includeLeagues) {
        List<LeagueResponseDTO> leagueDTOs = new ArrayList<>();
        long totalPlayers = 0;
        long totalTeams = 0;

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
            }
        }
        return SeasonResponseDTO.builder()
                .id(season.getId())
                .year(season.getYear())
                .status(season.getStatus())
                .leagues(leagueDTOs)
                .totalPlayers(totalPlayers)
                .totalTeams(totalTeams)
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
