package com.example.mauri.model.dto.create;

import com.example.mauri.enums.Sport;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlayerDTO {
    @NotBlank(message = "Meno je povinné")
    private String firstName;

    @NotBlank(message = "Priezvisko je povinné")
    private String lastName;

    @Email(message = "Nesprávny formát emailovej adresy")
    private String email;

    @Pattern(regexp = "^$|^[0-9+ ]{7,15}$", message = "Nesprávny formát telefónneho čísla")
    private String phone;

    @NotEmpty(message = "Aspoň jeden šport je povinný")
    private Set<Sport> sports=new HashSet<>();
}
