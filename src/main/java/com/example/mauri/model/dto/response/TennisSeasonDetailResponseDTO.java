package com.example.mauri.model.dto.response;

import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.model.dto.request.SeasonTennisLeagueSummaryDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TennisSeasonDetailResponseDTO {
    private String id;
    private int year;
    private SeasonStatus status;

    private long totalLeagues;
    private long totalPlayers;
    private long totalTeams;
    private long totalParticipants;

    private long totalMatches;
    private long totalFinishedMatches;
    private long totalScratchedMatches;
    private long totalCancelledMatches;
    private long totalCompletedMatches;

    private List<SeasonTennisLeagueSummaryDTO> leagues;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;
}
