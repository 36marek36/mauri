package com.example.mauri.service;

import com.example.mauri.model.Team;
import lombok.NonNull;

import java.util.List;

public interface TeamService {

    List<Team> getActiveTeams();
    List<Team> getInactiveTeams();
    Team getTeamById(@NonNull String id);
    Team createTeam(String player1Id, String player2Id);
    String deleteTeam(String id);
    List<Team> getActiveTeamsNotInAnyActiveLeague();
    void deactivateTeam(@NonNull String id);

}
