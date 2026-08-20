package com.example.mauri.model.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class VolleyMatchActivityDTO {
    private VolleyMatchResponseDTO match;
    private Instant playedAt;
    private String leagueName;
}
