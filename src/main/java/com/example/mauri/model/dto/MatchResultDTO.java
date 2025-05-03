package com.example.mauri.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultDTO {
    private String matchId;
    private String scratchedId;
    private Integer score1;
    private Integer score2;
}
