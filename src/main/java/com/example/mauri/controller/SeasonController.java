package com.example.mauri.controller;

import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.model.Season;
import com.example.mauri.model.dto.request.AddLeagueToSeasonDTO;
import com.example.mauri.model.dto.create.CreateSeasonDTO;
import com.example.mauri.model.dto.response.*;
import com.example.mauri.model.dto.update.UpdateSeasonDTO;
import com.example.mauri.service.SeasonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/tennis")
    public ResponseEntity<List<TennisSeasonListResponseDTO>> getTennisSeasons(@RequestParam(required = false) List<SeasonStatus> status) {
        return ResponseEntity.ok(seasonService.getTennisSeasonsList(status));
    }

    @GetMapping("/tennis/{seasonId}")
    public ResponseEntity<TennisSeasonDetailResponseDTO> getTennisSeasonDetail(@PathVariable String seasonId) {
        return ResponseEntity.ok(seasonService.getTennisSeasonDetail(seasonId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/current")
    public ResponseEntity<SeasonManagementResponseDTO> getCurrentManagedSeason() {
        return ResponseEntity.ok(
                seasonService.getCurrentManagedSeason()
        );
    }

    @GetMapping("/volley")
    public ResponseEntity<List<VolleySeasonListResponseDTO>> getVolleySeasons(@RequestParam(required = false) List<SeasonStatus> status) {
        return ResponseEntity.ok(seasonService.getVolleySeasons(status));
    }

    @GetMapping("/current/exists")
    public ResponseEntity<Boolean> isSeasonActive() {
        boolean exists = seasonService.isSeasonActive();
        return ResponseEntity.ok(exists);
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

    @PatchMapping("/{id}")
    public ResponseEntity<SeasonResponseDTO> updateSeason(@PathVariable String id,
                                                          @RequestBody UpdateSeasonDTO updateSeasonDTO) {
        SeasonResponseDTO updated = seasonService.updateSeason(id, updateSeasonDTO);
        return ResponseEntity.ok(updated);
    }
}
