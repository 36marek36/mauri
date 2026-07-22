package com.example.mauri.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity (name = "volleyball_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolleyTeam {

    @Id
    private String id;

    private String name;

    @ManyToOne
    private Player captain;

    @ElementCollection
    private Set<String> players = new HashSet<>();

    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }
}
