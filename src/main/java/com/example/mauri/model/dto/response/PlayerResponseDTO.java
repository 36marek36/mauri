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
public class PlayerResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String name;
    private String email;
    private String phone;
    private LocalDate registrationDate;
    private LocalDate deletedDate;
    private boolean active;
}
