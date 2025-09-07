package com.example.mauri.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponseDTO {
    private String id;
    private String name;
    private PlayerResponseDTO player1;
    private PlayerResponseDTO player2;
    private LocalDate registrationDate;
    private LocalDate deletedDate;
    private boolean active;
}
