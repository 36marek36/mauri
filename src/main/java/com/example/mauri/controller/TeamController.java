package com.example.mauri.controller;

import com.example.mauri.model.Team;
import com.example.mauri.model.dto.CreateTeamDTO;
import com.example.mauri.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {
    private final TeamService teamService;


    @GetMapping("/")
    List<Team> getAllTeams() {
        return teamService.getTeams();
    }

    @GetMapping("/{id}")
    Team getTeamById(@PathVariable String id) {
        return teamService.getTeamById(id);
    }

    @GetMapping("/not-in-any-active-league")
    public ResponseEntity<List<Team>> getFreeTeams() {
        List<Team> freeTeams = teamService.getTeamsNotInAnyActiveLeague();
        return new ResponseEntity<>(freeTeams, HttpStatus.OK);
    }

    @PostMapping("/create")
    ResponseEntity<Team> createTeam(@RequestBody CreateTeamDTO team) {
        Team created = teamService.createTeam(team.getPlayer1Id(), team.getPlayer2Id());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Team> deleteTeam(@PathVariable String id) {
        teamService.deleteTeam(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
