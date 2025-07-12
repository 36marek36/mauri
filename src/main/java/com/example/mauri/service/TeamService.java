package com.example.mauri.service;

import com.example.mauri.model.Team;
import lombok.NonNull;

import java.util.List;

public interface TeamService {

    List<Team> getTeams();
    Team getTeamById(@NonNull String id);
    Team createTeam(String player1Id, String player2Id);
    void deleteTeam(@NonNull String id);
    List<Team> getTeamsNotInAnyActiveLeague();
}
