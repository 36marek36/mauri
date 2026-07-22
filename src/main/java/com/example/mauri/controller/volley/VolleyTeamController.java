package com.example.mauri.controller.volley;

import com.example.mauri.model.dto.create.CreateVolleyTeamDTO;
import com.example.mauri.model.dto.request.VolleyTeamPlayerNameRequest;
import com.example.mauri.model.dto.response.VolleyTeamResponseDTO;
import com.example.mauri.service.volley.VolleyTeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/volleyball/teams")
@RequiredArgsConstructor
public class VolleyTeamController {

    private final VolleyTeamService volleyTeamService;

    @GetMapping("/")
    public ResponseEntity<List<VolleyTeamResponseDTO>> getAllTeams() {
        List<VolleyTeamResponseDTO> teams = volleyTeamService.getVolleyTeams();
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/create")
    public ResponseEntity<VolleyTeamResponseDTO> createVolleyTeam(@Valid @RequestBody CreateVolleyTeamDTO volleyTeamResponseDTO) {
        VolleyTeamResponseDTO created = volleyTeamService.createVolleyTeam(volleyTeamResponseDTO.getName(), volleyTeamResponseDTO.getCaptainId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVolleyTeam(@PathVariable String id) {
        volleyTeamService.deleteVolleyTeam(id);
        return ResponseEntity.ok("Team deleted successfully");
    }

    @PatchMapping("/{teamId}/add")
    public ResponseEntity<String> addPlayerToTeam(@PathVariable String teamId, @RequestBody VolleyTeamPlayerNameRequest playerName) {
        volleyTeamService.addPlayerToTeam(teamId, playerName);
        return ResponseEntity.ok("Player added successfully");
    }

    @PatchMapping("/{teamId}/remove")
    public ResponseEntity<String> removePlayerFromTeam(@PathVariable String teamId, @RequestBody VolleyTeamPlayerNameRequest playerName) {
        volleyTeamService.removePlayerFromTeam(teamId, playerName);
        return ResponseEntity.ok("Player removed successfully");
    }
}
