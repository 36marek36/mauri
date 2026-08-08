package com.example.mauri.model;

import com.example.mauri.enums.Sport;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate registrationDate;
    private LocalDate deletedDate;
    private boolean active;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "player_sports", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "sport")
    private Set<Sport> sports = new HashSet<>();

    @PrePersist
    protected void onRegister() {
        registrationDate = LocalDate.now();
        active = true;
    }

}
