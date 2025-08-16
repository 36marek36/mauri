package com.example.mauri.model;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name = "leagues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class League {

    @Id
    private String id;

    private String name;

    @Enumerated(EnumType.STRING)
    private MatchType leagueType;

    @ManyToOne
    @JoinColumn(name = ("season_id"))
    @JsonBackReference
    private Season season;

    @ManyToMany
    @JoinTable(
            name = ("league_players"),
            joinColumns = @JoinColumn(name = ("league_id")),
            inverseJoinColumns = @JoinColumn(name = ("player_id"))
    )
    private List<Player> players;

    @ManyToMany
    @JoinTable(
            name = ("league_teams"),
            joinColumns = @JoinColumn(name = ("league_id")),
            inverseJoinColumns = @JoinColumn(name = ("team_id"))
    )
    private List<Team> teams;

    @Enumerated(EnumType.STRING)
    private LeagueStatus status;

    @PrePersist
    protected void onCreate() {
        status = LeagueStatus.CREATED;
    }

}
