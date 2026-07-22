package com.example.mauri.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVolleyTeamDTO {
    @NotBlank(message = "Názov tímu je povinný")
    private String name;
    @NotBlank(message = "Vyber kapitána tímu")
    private String captainId;
}
