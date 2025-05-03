package com.example.mauri.model;

import com.example.mauri.enums.MatchType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    private String id;

    @Enumerated
    private MatchType matchType;

    @ManyToOne
    private Player player1;

    @ManyToOne
    private Player player2;

    @ManyToOne
    private Team team1;

    @ManyToOne
    private Team team2;

    private String leagueId;

    @OneToMany(mappedBy = "match")
    private List<SetResult> results = new ArrayList<>();

}
