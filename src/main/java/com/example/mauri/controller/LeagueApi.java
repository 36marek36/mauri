package com.example.mauri.controller;

import com.example.mauri.model.League;
import com.example.mauri.model.dto.CreateLeagueDTO;
import com.example.mauri.service.LeagueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/leagues")
@Slf4j
public class LeagueApi {

    private final LeagueService leagueService;

    @Autowired
    public LeagueApi(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @GetMapping("/")
    public List<League> getLeagues() {
        return leagueService.getAllLeagues();
    }

    @GetMapping("/{id}")
    public League getLeagueById(@PathVariable("id") String id) {
        return leagueService.getLeagueById(id);
    }

    @PostMapping("/create")
    ResponseEntity<League> createLeague(@RequestBody CreateLeagueDTO createLeagueDTO) {
        League createdLeague = leagueService.createLeague(createLeagueDTO);
        return new ResponseEntity<>(createdLeague, HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    ResponseEntity<League> deleteLeague(@PathVariable("id") String id) {
        leagueService.deleteLeagueById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
