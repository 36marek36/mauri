package com.example.mauri.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueDTO {
    private String leagueName;
    private Integer seasonYear;
}
