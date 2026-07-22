package com.example.mauri.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVolleyLeagueDTO {
    @NotBlank(message = "Názov ligy je povinný")
    private String leagueName;
    private String seasonId;
}
