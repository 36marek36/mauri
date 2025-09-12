package com.example.mauri.model.dto.response;

import com.example.mauri.model.dto.request.TeamShortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate registrationDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate deletedDate;
    private boolean active;
    private List<TeamShortDTO> teams;
}
