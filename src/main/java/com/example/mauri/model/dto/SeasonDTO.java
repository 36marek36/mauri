package com.example.mauri.model.dto;

import com.example.mauri.enums.SeasonStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SeasonDTO {
    private String id;
    private int year;
    private SeasonStatus status;
    private List<LeagueDTO> leagues;

    private long totalPlayers;
    private long totalTeams;
}
