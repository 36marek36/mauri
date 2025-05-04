package com.example.mauri.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetScore {
    private Integer setNumber;
    private Integer score1;
    private Integer score2;
}
