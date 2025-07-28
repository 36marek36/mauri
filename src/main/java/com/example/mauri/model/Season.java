package com.example.mauri.model;

import com.example.mauri.enums.SeasonStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name = "seasons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Season {

    @Id
    private String id;

    private int year;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<League> leagues;

    @Enumerated(EnumType.STRING)
    private SeasonStatus status;
}
