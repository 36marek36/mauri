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
import java.util.Map;

@RestController
@RequestMapping("/rest/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {
    private final TeamService teamService;


    @GetMapping("/")
    List<Team> getActiveTeams() {
        return teamService.getActiveTeams();
    }

    @GetMapping("/inactive")
    List<Team> getInactiveTeams() {
        return teamService.getInactiveTeams();
    }

    @GetMapping("/{id}")
    Team getTeamById(@PathVariable String id) {
        return teamService.getTeamById(id);
    }

    @GetMapping("/not-in-any-active-league")
    public ResponseEntity<List<Team>> getFreeTeams() {
        List<Team> freeTeams = teamService.getActiveTeamsNotInAnyActiveLeague();
        return new ResponseEntity<>(freeTeams, HttpStatus.OK);
    }

    @PostMapping("/create")
    ResponseEntity<Team> createTeam(@RequestBody CreateTeamDTO team) {
        Team created = teamService.createTeam(team.getPlayer1Id(), team.getPlayer2Id());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTeam(@PathVariable String id) {
        String status = teamService.deleteTeam(id);

        Map<String,String> response = Map.of("status", status);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
