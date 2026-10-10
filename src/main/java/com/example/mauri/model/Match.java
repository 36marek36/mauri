package com.example.mauri.model;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "round_number")
    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    private boolean ratingCalculated;

    @Column(name = "home_player_rating_before")
    private Integer homePlayerRatingBefore;

    @Column(name = "away_player_rating_before")
    private Integer awayPlayerRatingBefore;

    @Column(name = "home_team_player1_double_rating_before")
    private Integer homeTeamPlayer1DoubleRatingBefore;

    @Column(name = "home_team_player2_double_rating_before")
    private Integer homeTeamPlayer2DoubleRatingBefore;

    @Column(name = "away_team_player1_double_rating_before")
    private Integer awayTeamPlayer1DoubleRatingBefore;

    @Column(name = "away_team_player2_double_rating_before")
    private Integer awayTeamPlayer2DoubleRatingBefore;
    @PrePersist
    protected void onCreate() {
        status = MatchStatus.CREATED;
        ratingCalculated = false;
    }

}
