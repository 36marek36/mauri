package com.example.mauri.model.dto.create;

import com.example.mauri.enums.MatchType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeagueDTO {
    @NotBlank(message = "Názov ligy je povinný")
    private String name;
    private MatchType leagueType;
    private String seasonId;
}
