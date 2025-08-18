package com.example.mauri.model.dto.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlayerDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
