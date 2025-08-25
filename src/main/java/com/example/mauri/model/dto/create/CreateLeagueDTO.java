package com.example.mauri.model.dto.create;

import com.example.mauri.enums.MatchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeagueDTO {
    @NotBlank(message = "Názov ligy je povinný")
    private String name;
    @NotNull(message = "Typ ligy je povinný.")
    private MatchType leagueType;
    private String seasonId;
}
