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

    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    @ManyToOne
    @JoinColumn (name = ("home_player_id"))
    private Player homePlayer;

    @ManyToOne
    @JoinColumn (name = ("away_player_id"))
    private Player awayPlayer;

    @ManyToOne
    @JoinColumn (name = ("home_team_id"))
    private Team homeTeam;

    @ManyToOne
    @JoinColumn (name = ("away_team_id"))
    private Team awayTeam;

    private String leagueId;

    @Embedded
    private MatchResult result;

}
