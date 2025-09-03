package com.example.mauri.model.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlayerDTO {
    @NotBlank(message = "Meno je povinné")
    private String firstName;

    @NotBlank(message = "Priezvisko je povinné")
    private String lastName;

    @Email(message = "Nesprávny formát emailovej adresy")
    private String email;

    @Pattern(regexp = "^$|^[0-9+ ]{7,15}$", message = "Nesprávny formát telefónneho čísla")
    private String phone;
}
