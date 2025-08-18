package com.example.mauri.model.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeasonDTO {

    @NotNull(message = "Zadaj rok sezóny!")
    private Integer year;
}
