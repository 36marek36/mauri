package com.example.mauri.mapper;

import com.example.mauri.model.League;
import com.example.mauri.model.Season;
import com.example.mauri.model.dto.response.LeagueResponseDTO;
import com.example.mauri.model.dto.response.SeasonResponseDTO;
import com.example.mauri.service.LeagueService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
            for (League league : season.getLeagues()) {
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
}
