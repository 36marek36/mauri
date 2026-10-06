package com.example.mauri.service;

import com.example.mauri.model.Player;

public interface PlayerRatingService {
    Integer getRating(String playerId);

    void createRatingIfNotExists(Player player);

    int calculateNewRating(int playerRating,int opponentRating,double actualScore);

    void updateRatingsAfterMatch(String matchId);

    void updatePlayerRatings(Player winner, Player loser);

    void initializeRatingsForLeague(String leagueId, int ratingValue);

    void recalculateRatingsForLeague(String leagueId);

    void resetRatingCalculationForLeague(String leagueId);

}
