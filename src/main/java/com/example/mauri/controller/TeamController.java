package com.example.mauri.controller;

import com.example.mauri.model.dto.create.CreateTeamDTO;
import com.example.mauri.model.dto.response.TeamResponseDTO;
import com.example.mauri.service.TeamService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<TeamResponseDTO>> getActiveTeams() {
        List<TeamResponseDTO> teams = teamService.getActiveTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<TeamResponseDTO>> getInactiveTeams() {
        List<TeamResponseDTO> teams = teamService.getInactiveTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> getTeamById(@PathVariable String id) {
        TeamResponseDTO team = teamService.getTeamResponseById(id);
        return ResponseEntity.ok(team);
    }

    @GetMapping("/not-in-any-active-league")
    public ResponseEntity<List<TeamResponseDTO>> getFreeTeams() {
        List<TeamResponseDTO> freeTeamsDTO = teamService.getActiveTeamsNotInAnyActiveLeague();
        return ResponseEntity.ok(freeTeamsDTO);
    }

    @GetMapping("/not-in-league/{leagueId}")
    public ResponseEntity<List<TeamResponseDTO>> getTeamsNotInLeague(@PathVariable String leagueId) {
        List<TeamResponseDTO> teams = teamService.getTeamsNotInLeague(leagueId);
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/create")
    ResponseEntity<TeamResponseDTO> createTeam(@Valid @RequestBody CreateTeamDTO team) {
        TeamResponseDTO created = teamService.createTeam(team.getPlayer1Id(), team.getPlayer2Id());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTeam(@PathVariable String id) {
        String status = teamService.deleteTeam(id);

        Map<String, String> response = Map.of("status", status);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
