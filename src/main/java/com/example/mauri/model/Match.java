package com.example.mauri.model;

import com.example.mauri.enums.MatchType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Integer score1;
    private Integer score2;
    private String scratchedId;
    private String winnerId;

}
