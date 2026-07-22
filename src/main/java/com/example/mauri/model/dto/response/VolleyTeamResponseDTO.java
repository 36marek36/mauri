package com.example.mauri.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolleyTeamResponseDTO {
    private String id;
    private String name;
    private PlayerResponseDTO captain;
    private LocalDate createdAt;
    private Set<String> players;
}
