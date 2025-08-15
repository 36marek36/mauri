package com.example.mauri.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = ("player1_id"))
    private Player player1;

    @ManyToOne
    @JoinColumn(name = ("player2_id"))
    private Player player2;

    private LocalDate deletedDate;
    private boolean active;
}
