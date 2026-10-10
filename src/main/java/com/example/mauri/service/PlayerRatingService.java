package com.example.mauri.service;

import com.example.mauri.model.Player;
import com.example.mauri.model.dto.response.PlayerRatingResponseDTO;

import java.util.List;

public interface PlayerRatingService {
    Integer getRating(String playerId);

    void createRatingIfNotExists(Player player);

    List<PlayerRatingResponseDTO> getRatingRanking();

    int calculateNewRating(int playerRating, int opponentRating, double actualScore,double marginMultiplier);

    void updateRatingsAfterMatch(String matchId);

    void setPlayerRating(String playerId, int ratingValue);

    void initializeRatingsForLeague(String leagueId, int ratingValue);

    void recalculateRatingsForLeague(String leagueId);

    void resetRatingCalculationForLeague(String leagueId);

}
