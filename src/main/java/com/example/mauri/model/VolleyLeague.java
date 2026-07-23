package com.example.mauri.model;

import com.example.mauri.enums.LeagueStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "volleyball_leagues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolleyLeague {
    @Id
    private String id;
    private String name;

    @ManyToOne
    @JoinColumn(name = ("season_id"))
    private Season season;

    @ManyToMany
    @JoinTable(name = ("volley_league_teams"),
            joinColumns = @JoinColumn(name = ("volley_league_id")),
            inverseJoinColumns = @JoinColumn(name = ("volley_team_id")))
    @Builder.Default
    private Set<VolleyTeam> teams = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private LeagueStatus status;

    @PrePersist
    protected void onCreate() {
        status = LeagueStatus.CREATED;
    }
}
