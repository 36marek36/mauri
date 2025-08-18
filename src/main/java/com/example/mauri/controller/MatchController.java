package com.example.mauri.controller;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.model.Match;
import com.example.mauri.model.dto.create.CreateMatchDTO;
import com.example.mauri.model.MatchResult;
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
    List<Match> getMatches() {
        return matchService.getMatches();
    }

    @GetMapping("/{id}")
    Match getMatch(@PathVariable String id) {
        return matchService.getMatch(id);
    }

    @GetMapping("/{leagueId}/grouped-by-round")
    public ResponseEntity<Map<Integer, List<Match>>> getMatchesGroupedByRound(@PathVariable String leagueId) {
        Map<Integer, List<Match>> groupedMatches = matchService.getMatchesGroupedByRound(leagueId);
        return ResponseEntity.ok(groupedMatches);
    }

    @GetMapping("/player/{playerId}/status/{status}")
    public ResponseEntity<List<Match>> getMatchesByPlayerInActiveSeason(
            @PathVariable String playerId,
            @PathVariable MatchStatus status) {

        List<Match> matches = matchService.getMatchesForPlayerInActiveSeason(playerId, status);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/team/{teamId}/status/{status}")
    public ResponseEntity<List<Match>> getMatchesByTeamInActiveSeason(
            @PathVariable String teamId,
            @PathVariable MatchStatus status) {

        List<Match> matches = matchService.getMatchesForTeamInActiveSeason(teamId, status);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/create")
    ResponseEntity<Match> createMatch(@RequestBody CreateMatchDTO createMatchDTO) {
        Match created = matchService.createMatch(createMatchDTO);
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
    ResponseEntity<?> generateMatch(@PathVariable String leagueId) {
        try {
            List<Match> matches = matchService.generateMatchesForLeague(leagueId);
            return new ResponseEntity<>(matches, HttpStatus.OK);
        }catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PatchMapping("/{matchId}/cancel-result")
    ResponseEntity<?> cancelMatchResult(@PathVariable String matchId) {
        matchService.cancelResult(matchId);
        return ResponseEntity.ok().body("Match cancelled");
    }

}
