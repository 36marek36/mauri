package com.example.mauri.controller;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.model.Match;
import com.example.mauri.model.dto.create.CreateMatchDTO;
import com.example.mauri.model.MatchResult;
import com.example.mauri.model.dto.response.MatchResponseDTO;
import com.example.mauri.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchService matchService;


    @GetMapping("/")
    public ResponseEntity<List<MatchResponseDTO>> getAllMatches() {
        List<MatchResponseDTO> matches = matchService.getMatches();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponseDTO> getMatch(@PathVariable String id) {
        MatchResponseDTO matchDTO = matchService.getMatch(id);
        return ResponseEntity.ok(matchDTO);
    }

    @GetMapping("/{leagueId}/grouped-by-round")
    public ResponseEntity<Map<Integer, List<MatchResponseDTO>>> getMatchesGroupedByRound(@PathVariable String leagueId) {
        Map<Integer, List<MatchResponseDTO>> groupedMatches = matchService.getMatchesGroupedByRound(leagueId);
        return ResponseEntity.ok(groupedMatches);
    }

    @GetMapping("/player/{playerId}/status/{status}")
    public ResponseEntity<List<MatchResponseDTO>> getPlayerMatchesInActiveSeason(
            @PathVariable String playerId,
            @PathVariable MatchStatus status) {

        List<MatchResponseDTO> matches = matchService.getMatchesForPlayerInActiveSeason(playerId, status);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/team/{teamId}/status/{status}")
    public ResponseEntity<List<MatchResponseDTO>> getTeamMatchesInActiveSeason(
            @PathVariable String teamId,
            @PathVariable MatchStatus status) {

        List<MatchResponseDTO> matches = matchService.getMatchesForTeamInActiveSeason(teamId, status);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/create")
    public ResponseEntity<MatchResponseDTO> createMatch(@RequestBody CreateMatchDTO createMatchDTO) {
        MatchResponseDTO created = matchService.createMatch(createMatchDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Match> deleteMatch(@PathVariable String id) {
        matchService.deleteMatch(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{matchId}/result")
    ResponseEntity<Match> addMatchResult(@PathVariable String matchId, @RequestBody MatchResult matchResultDTO) {
        Match matchResult = matchService.addResult(matchId, matchResultDTO);
        return new ResponseEntity<>(matchResult, HttpStatus.OK);
    }

    @PatchMapping("/{leagueId}/generate-matches")
    public ResponseEntity<List<MatchResponseDTO>> generateMatch(@PathVariable String leagueId) {
        List<MatchResponseDTO> matches = matchService.generateMatchesForLeague(leagueId);
        return ResponseEntity.ok(matches);
    }

    @PatchMapping("/{matchId}/cancel-result")
    ResponseEntity<?> cancelMatchResult(@PathVariable String matchId) {
        matchService.cancelResult(matchId);
        return ResponseEntity.ok().body("Match cancelled");
    }

}
