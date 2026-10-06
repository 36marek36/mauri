package com.example.mauri.service;

import com.example.mauri.model.Player;

public interface PlayerRatingService {
    Integer getRating (String playerId);
    void createRatingIfNotExists(Player player);
}
