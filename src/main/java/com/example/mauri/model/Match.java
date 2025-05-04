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
    private Player homePlayer;

    @ManyToOne
    private Player awayPlayer;

    @ManyToOne
    private Team homeTeam;

    @ManyToOne
    private Team awayTeam;

    private String leagueId;

    @Embedded
    private MatchResult result;

}
