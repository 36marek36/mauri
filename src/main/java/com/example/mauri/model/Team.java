package com.example.mauri.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = ("player1_id"))
    private Player player1;

    @ManyToOne
    @JoinColumn(name = ("player2_id"))
    private Player player2;

    private LocalDate createdAt;
    private LocalDate deletedDate;
    private boolean active;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        active = true;
    }
}
