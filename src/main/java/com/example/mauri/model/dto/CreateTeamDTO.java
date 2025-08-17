package com.example.mauri.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamDTO {

    @NotBlank(message = "Nevybral si prvého hráča tímu.")
    private String player1Id;
    @NotBlank(message = "Nevybral si druhého hráča tímu.")
    private String player2Id;
}
