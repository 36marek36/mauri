package com.example.mauri.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity (name = "volleyball_matches")
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

    @Embedded
    private VolleyMatchResult result;

}
