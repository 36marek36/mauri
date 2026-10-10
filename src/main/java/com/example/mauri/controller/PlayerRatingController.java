package com.example.mauri.controller;

import com.example.mauri.enums.MatchType;
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
    public int calculateRating(@RequestParam int playerRating, @RequestParam int opponentRating, @RequestParam double actualScore, @RequestParam double marginMultiplier) {
        return playerRatingService.calculateNewRating(playerRating, opponentRating, actualScore, marginMultiplier);
    }

    @GetMapping("/ratings")
    public List<PlayerRatingResponseDTO> getPlayerRatings(
            @RequestParam(defaultValue = "SINGLES") MatchType matchType) {
        return playerRatingService.getRatingsByType(matchType);
    }

    @PatchMapping("/{playerId}/rating")
    public ResponseEntity<Void> setPlayerRating(@PathVariable String playerId, @RequestParam int rating) {
        playerRatingService.setPlayerRating(playerId, rating);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{playerId}/double-rating")
    public ResponseEntity<Void> setPlayerDoubleRating(@PathVariable String playerId, @RequestParam int rating) {
        playerRatingService.setPlayerDoubleRating(playerId, rating);
        return ResponseEntity.ok().build();
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
