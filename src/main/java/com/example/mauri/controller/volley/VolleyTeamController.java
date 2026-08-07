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
import java.util.Map;

@RestController
@RequestMapping("/rest/volleyball/teams")
@RequiredArgsConstructor
public class VolleyTeamController {

    private final VolleyTeamService volleyTeamService;

    @GetMapping("/")
    public ResponseEntity<List<VolleyTeamResponseDTO>> getActiveTeams() {
        List<VolleyTeamResponseDTO> teams = volleyTeamService.getActiveVolleyTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<VolleyTeamResponseDTO>> getInactiveTeams() {
        List<VolleyTeamResponseDTO> teams = volleyTeamService.getInactiveVolleyTeams();
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/create")
    public ResponseEntity<VolleyTeamResponseDTO> createVolleyTeam(@Valid @RequestBody CreateVolleyTeamDTO volleyTeamResponseDTO) {
        VolleyTeamResponseDTO created = volleyTeamService.createVolleyTeam(volleyTeamResponseDTO.getName(), volleyTeamResponseDTO.getCaptainId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String,String>> deleteVolleyTeam(@PathVariable String id) {
        String status = volleyTeamService.deleteVolleyTeam(id);
        Map<String, String> response = Map.of("status", status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
