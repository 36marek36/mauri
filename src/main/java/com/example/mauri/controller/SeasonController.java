package com.example.mauri.controller;

import com.example.mauri.model.Season;
import com.example.mauri.model.dto.AddLeagueToSeasonDTO;
import com.example.mauri.model.dto.CreateSeasonDTO;
import com.example.mauri.model.dto.SeasonDTO;
import com.example.mauri.service.SeasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/seasons")
@RequiredArgsConstructor
@Slf4j
public class SeasonController {

    private final SeasonService seasonService;

    @GetMapping("/")
    public List<SeasonDTO> getSeasons() {
        return seasonService.getSeasons();
    }

//    @GetMapping("/{seasonId}")
//    public Season getSeasonById(@PathVariable String seasonId) {
//        return seasonService.getSeason(seasonId);
//    }

    @GetMapping("/{seasonId}/stats")
    public SeasonDTO getSeasonStats(@PathVariable("seasonId") String seasonId) {
        return seasonService.getSeasonStats(seasonId);
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

    @PatchMapping("/{seasonId}/start")
    public ResponseEntity<String> startSeason(@PathVariable String seasonId) {
        String message = seasonService.startSeason(seasonId);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/{seasonId}/finish")
    public ResponseEntity<String> finishSeason(@PathVariable String seasonId) {
        String message = seasonService.finishSeason(seasonId);
        return ResponseEntity.ok(message);
    }
}
