package com.example.mauri.controller;

import com.example.mauri.model.Season;
import com.example.mauri.model.dto.request.AddLeagueToSeasonDTO;
import com.example.mauri.model.dto.create.CreateSeasonDTO;
import com.example.mauri.model.dto.response.SeasonResponseDTO;
import com.example.mauri.service.SeasonService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<SeasonResponseDTO>> getSeasons() {
        List<SeasonResponseDTO> seasons = seasonService.getSeasons();
        if (seasons.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(seasons);
    }

    @GetMapping("/{seasonId}/stats")
    public ResponseEntity<SeasonResponseDTO> getSeasonStats(@PathVariable String seasonId) {
        SeasonResponseDTO seasonStats = seasonService.getSeasonStats(seasonId);
        return ResponseEntity.ok(seasonStats);
    }

    @GetMapping("/current")
    public ResponseEntity<SeasonResponseDTO> getCurrentSeason() {
        SeasonResponseDTO current = seasonService.getCurrentSeason();
        return ResponseEntity.ok(current);
    }

    @PostMapping("/create")
    public ResponseEntity<SeasonResponseDTO> createSeason(@Valid @RequestBody CreateSeasonDTO createSeasonDTO) {
        SeasonResponseDTO season = seasonService.createSeason(createSeasonDTO);
        return new ResponseEntity<>(season, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Season> deleteSeason(@PathVariable String id) {
        seasonService.deleteSeason(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{seasonId}/addLeague")
    public ResponseEntity<String> addLeagueToSeason(
            @PathVariable String seasonId,
            @RequestBody AddLeagueToSeasonDTO addLeagueToSeasonDTO) {

        String message = seasonService.addLeagueToSeason(addLeagueToSeasonDTO.getLeagueId(), seasonId);
        return ResponseEntity.ok(message);
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
