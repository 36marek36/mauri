package com.example.mauri.model;

import com.example.mauri.enums.MatchType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name = "leagues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class League {

    @Id
    private String id;

    private String name;

    @Enumerated
    private MatchType leagueType;

    @ManyToMany
    private List<Player> players;

    @ManyToMany
    private List<Team> teams;

    @OneToMany
    private List<Match> matches;

}
