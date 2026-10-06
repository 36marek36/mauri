package com.example.mauri.controller;

import com.example.mauri.model.dto.response.PlayerRatingResponseDTO;
import com.example.mauri.service.PlayerRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/player_rating")
@RequiredArgsConstructor
public class PlayerRatingController {
    private final PlayerRatingService playerRatingService;

    @GetMapping("/calculate")
    public int calculateRating(@RequestParam int playerRating, @RequestParam int opponentRating, @RequestParam double actualScore) {
        return playerRatingService.calculateNewRating(playerRating, opponentRating, actualScore);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<PlayerRatingResponseDTO>> getRatingRanking() {
        return ResponseEntity.ok(playerRatingService.getRatingRanking());
    }

    @PatchMapping("/{matchId}/update-rating")
    public ResponseEntity<Void> updateRating(@PathVariable String matchId) {
        playerRatingService.updateRatingsAfterMatch(matchId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/initialize/{leagueId}")
    public ResponseEntity<Void> initializeRatings(
            @PathVariable String leagueId,
            @RequestParam int rating) {

        playerRatingService.initializeRatingsForLeague(leagueId, rating);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/recalculate/{leagueId}")
    public ResponseEntity<Void> recalculateLeague(
            @PathVariable String leagueId) {

        playerRatingService.recalculateRatingsForLeague(leagueId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reset/{leagueId}")
    public ResponseEntity<Void> resetRatingCalculation(
            @PathVariable String leagueId) {

        playerRatingService.resetRatingCalculationForLeague(leagueId);

        return ResponseEntity.ok().build();
    }
}
