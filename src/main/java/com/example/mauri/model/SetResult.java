package com.example.mauri.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetResult {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private int setNumber; //1-5

    private int scorePlayer1;
    private int scorePlayer2;

}
