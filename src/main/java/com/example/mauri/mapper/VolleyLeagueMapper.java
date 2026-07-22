package com.example.mauri.mapper;

import com.example.mauri.model.VolleyLeague;
import com.example.mauri.model.dto.response.VolleyLeagueResponseDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class VolleyLeagueMapper {
    public VolleyLeagueResponseDTO mapToResponseDTO (VolleyLeague league) {
        return VolleyLeagueResponseDTO.builder()
                .leagueId(league.getId())
                .leagueName(league.getName())
                .seasonYear(league.getSeason() !=null? league.getSeason().getYear() : null)
                .leagueStatus(league.getStatus())
                .build();
    }
}
