package com.example.mauri.controller;

import com.example.mauri.model.Season;
import com.example.mauri.model.dto.AddLeagueToSeasonDTO;
import com.example.mauri.model.dto.CreateSeasonDTO;
import com.example.mauri.service.SeasonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/seasons")
@Slf4j
public class SeasonApi {

    private final SeasonService seasonService;

    @Autowired
    public SeasonApi(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @GetMapping("/")
    public List<Season> getSeasons() {
        return seasonService.getSeasons();
    }

    @GetMapping("/{seasonId}")
    public Season getSeasonById(@PathVariable String seasonId) {
        return seasonService.getSeason(seasonId);
    }

    @PostMapping("/create")
    public ResponseEntity<Season> createSeason(@RequestBody CreateSeasonDTO createSeasonDTO) {
        Season season = seasonService.createSeason(createSeasonDTO);
        return new ResponseEntity<>(season, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Season> deleteSeason(@PathVariable String id) {
        seasonService.deleteSeason(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{seasonId}/addLeague")
    public ResponseEntity<Season> addLeagueToSeason(@PathVariable String seasonId, @RequestBody AddLeagueToSeasonDTO addLeagueToSeasonDTO) {
        Season season = seasonService.addLeagueToSeason(addLeagueToSeasonDTO.getLeagueId(), seasonId);
        return new ResponseEntity<>(season, HttpStatus.OK);
    }
}
