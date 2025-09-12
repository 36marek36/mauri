package com.example.mauri.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate registrationDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate deletedDate;
    private boolean active;
}
