package com.example.mauri.mapper;

import com.example.mauri.model.VolleyTeam;
import com.example.mauri.model.dto.response.VolleyTeamResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class VolleyTeamMapper {

    public VolleyTeamResponseDTO mapToResponseDTO (VolleyTeam volleyTeam){
        return VolleyTeamResponseDTO.builder()
                .id(volleyTeam.getId())
                .name(volleyTeam.getName())
                .captain(volleyTeam.getCaptain() != null ? new PlayerMapper().mapToResponseDTO(volleyTeam.getCaptain()) : null)
                .createdAt(volleyTeam.getCreatedAt())
                .players(volleyTeam.getPlayers())
                .build();
    }
}
