package com.example.mauri.mapper;

import com.example.mauri.model.VolleyMatch;
import com.example.mauri.model.dto.response.VolleyMatchResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class VolleyMatchMapper {

    private final VolleyTeamMapper volleyTeamMapper;

    public VolleyMatchResponseDTO toVolleyMatchResponse(VolleyMatch volleyMatch) {
        return VolleyMatchResponseDTO.builder()
                .id(volleyMatch.getId())
                .volleyHomeTeam(volleyMatch.getHomeTeam() != null ? volleyTeamMapper.mapToResponseDTO(volleyMatch.getHomeTeam()) : null)
                .volleyAwayTeam(volleyMatch.getAwayTeam() != null ? volleyTeamMapper.mapToResponseDTO(volleyMatch.getAwayTeam()) : null)
                .result(volleyMatch.getResult() != null ? volleyMatch.getResult() : null)
                .build();
    }
}
