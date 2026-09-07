package com.example.mauri.model.dto.response;

import com.example.mauri.enums.SeasonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VolleySeasonListResponseDTO {
    private String id;
    private int year;
    private SeasonStatus status;

    private long totalLeagues;
    private long totalTeams;

    private long totalMatches;
    private long totalFinishedMatches;
    private long totalScratchedMatches;
    private long totalCancelledMatches;
    private long totalCompletedMatches;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;
}
