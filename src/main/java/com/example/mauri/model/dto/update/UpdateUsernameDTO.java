package com.example.mauri.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUsernameDTO {
    @NotBlank
    private String newUsername;
}
