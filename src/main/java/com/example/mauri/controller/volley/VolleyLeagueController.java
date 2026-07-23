package com.example.mauri.controller.volley;

import com.example.mauri.model.dto.create.CreateVolleyLeagueDTO;
import com.example.mauri.model.dto.request.AddParticipantsToLeagueDTO;
import com.example.mauri.model.dto.response.VolleyLeagueResponseDTO;
import com.example.mauri.service.volley.VolleyLeagueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/volleyball/volley_leagues")
@RequiredArgsConstructor
@Slf4j
public class VolleyLeagueController {

    private final VolleyLeagueService volleyLeagueService;

    @GetMapping("/")
    public ResponseEntity<List<VolleyLeagueResponseDTO>> getAllLeagues() {
        List<VolleyLeagueResponseDTO> leagues = volleyLeagueService.getLeagues();
        return ResponseEntity.ok(leagues);
    }

    @PostMapping("/create")
    public ResponseEntity<VolleyLeagueResponseDTO> createVolleyLeague (@RequestBody @Valid CreateVolleyLeagueDTO createVolleyLeagueDTO){
        VolleyLeagueResponseDTO createdLeague = volleyLeagueService.createLeague(createVolleyLeagueDTO);
        return new ResponseEntity<>(createdLeague, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVolleyLeague(@PathVariable String id) {
        volleyLeagueService.deleteLeague(id);
        return ResponseEntity.ok("League deleted successfully");
    }

    @PatchMapping("/{leagueId}/addTeams")
    public ResponseEntity<String> addTeamsToLeague(@PathVariable String leagueId, @RequestBody AddParticipantsToLeagueDTO dto) {
        String message = volleyLeagueService.addTeamsToLeague(leagueId, dto.getParticipantIds());
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/{leagueId}/removeTeam/{teamId}")
    public ResponseEntity<String> removeTeamFromLeague(@PathVariable String leagueId, @PathVariable String teamId) {
        String message = volleyLeagueService.removeTeamFromLeague(leagueId, teamId);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/{leagueId}/start")
    public ResponseEntity<String> startLeague(@PathVariable String leagueId) {
        String message = volleyLeagueService.startLeague(leagueId);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/{leagueId}/finish")
    public ResponseEntity<String> finishLeague(@PathVariable String leagueId) {
        String message = volleyLeagueService.finishLeague(leagueId);
        return ResponseEntity.ok(message);
    }

}
