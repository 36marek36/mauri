package com.example.mauri.controller.volley;

import com.example.mauri.model.VolleyMatch;
import com.example.mauri.model.VolleyMatchResult;
import com.example.mauri.model.dto.create.CreateVolleyMatchDTO;
import com.example.mauri.model.dto.response.VolleyMatchResponseDTO;
import com.example.mauri.service.volley.VolleyMatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/rest/volleyball/volley-matches")
public class VolleyMatchController {

    private final VolleyMatchService volleyMatchService;

    @GetMapping("/")
    public ResponseEntity<List<VolleyMatchResponseDTO>> getMatches() {
        List<VolleyMatchResponseDTO> matches = volleyMatchService.getMatches();
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/create")
    public ResponseEntity<VolleyMatchResponseDTO> createMatch(@RequestBody CreateVolleyMatchDTO createVolleyMatchDTO) {
        VolleyMatchResponseDTO created = volleyMatchService.createMatch(createVolleyMatchDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMatch(@PathVariable String id) {
        volleyMatchService.deleteMatch(id);
        return ResponseEntity.ok("Match deleted successfully");
    }

    @PatchMapping("/{matchId}/result")
    public ResponseEntity<VolleyMatch> addMatchResult(@PathVariable String matchId, @RequestBody VolleyMatchResult volleyMatchResult) {
        VolleyMatch match = volleyMatchService.addResult(matchId, volleyMatchResult);
        return new ResponseEntity<>(match, HttpStatus.OK);
    }
}
