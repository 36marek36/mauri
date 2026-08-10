package com.example.mauri.model.dto.update;

import com.example.mauri.enums.Sport;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlayerDTO {

    private String firstName;

    private String lastName;

    @Email(message = "Nesprávny formát emailovej adresy")
    private String email;

    @Pattern(regexp = "^$|^[0-9+ ]{7,15}$", message = "Nesprávny formát telefónneho čísla")
    private String phone;

    private Boolean active;

    @Size(min = 1, message = "Hráč musí mať vybraný aspoň jeden šport")
    private Set<Sport> sports = new HashSet<>();
}
