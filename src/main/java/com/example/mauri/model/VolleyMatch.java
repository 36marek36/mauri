package com.example.mauri.model;


import com.example.mauri.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "volleyball_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolleyMatch {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "home_team_id")
    private VolleyTeam homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id")
    private VolleyTeam awayTeam;

    private String volleyLeagueId;

    @Column(name = ("round_number"))
    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Embedded
    private VolleyMatchResult result;

    @PrePersist
    protected void onCreate() {
        status = MatchStatus.CREATED;
    }

}
