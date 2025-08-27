package com.example.mauri.model.dto.response;

import com.example.mauri.model.dto.request.ParticipantDTO;
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
    private ParticipantDTO player1;
    private ParticipantDTO player2;
    private LocalDate registrationDate;
    private LocalDate deletedDate;
    private boolean active;
}
