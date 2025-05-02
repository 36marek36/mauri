package com.example.mauri.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    private String id;

    @ManyToOne
    private Player player1;

    @ManyToOne
    private Player player2;
}
