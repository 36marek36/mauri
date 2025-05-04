package com.example.mauri.controller;

import com.example.mauri.model.Match;
import com.example.mauri.model.dto.CreateMatchDTO;
import com.example.mauri.model.MatchResult;
import com.example.mauri.service.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/matches")
@Slf4j
public class MatchApi {

    private final MatchService matchService;

    @Autowired
    public MatchApi(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/")
    List<Match> getMatches() {
        return matchService.getMatches();
    }

    @GetMapping("/{id}")
    Match getMatch(@PathVariable String id) {
        return matchService.getMatch(id);
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Match> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("handleIllegalArgumentException: {}", e.getMessage());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
