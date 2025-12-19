package com.example.mauri.model.dto.response;

import com.example.mauri.enums.SeasonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SeasonResponseDTO {
    private String id;
    private int year;
    private SeasonStatus status;
    private List<LeagueResponseDTO> leagues;

    private long totalPlayers;
    private long totalTeams;

    private LocalDate createdAt;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;
}
