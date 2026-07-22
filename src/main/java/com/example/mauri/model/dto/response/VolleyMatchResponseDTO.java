package com.example.mauri.model.dto.response;

import com.example.mauri.model.VolleyMatchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolleyMatchResponseDTO {
    private String id;

    private VolleyTeamResponseDTO volleyHomeTeam;
    private VolleyTeamResponseDTO volleyAwayTeam;
    private VolleyMatchResult result;
}
