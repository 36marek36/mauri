package com.example.mauri.model.dto.create;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVolleyMatchDTO {
    private String homeTeamId;
    private String awayTeamId;
    private String volleyLeagueId;
    private Integer roundNumber;
}
