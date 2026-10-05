package com.example.mauri.model.dto.response;

import com.example.mauri.enums.SeasonStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SeasonManagementResponseDTO {
    private String id;
    private int year;
    private SeasonStatus status;

    private List<LeagueManagementResponseDTO> leagues;
    private List<VolleyLeagueManagementResponseDTO> volleyLeagues;
}
