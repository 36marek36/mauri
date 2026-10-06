package com.example.mauri.service.impl;

import com.example.mauri.model.Player;
import com.example.mauri.model.PlayerRating;
import com.example.mauri.repository.PlayerRatingRepository;
import com.example.mauri.service.PlayerRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerRatingServiceBean implements PlayerRatingService {
    private final PlayerRatingRepository playerRatingRepository;

    @Override
    public Integer getRating(String playerId) {
        return playerRatingRepository.findByPlayerId(playerId)
                .map(PlayerRating::getRating)
                .orElse(null);
    }

    @Override
    public void createRatingIfNotExists(Player player) {
        if (playerRatingRepository.findByPlayerId(player.getId()).isEmpty()) {
            playerRatingRepository.save(PlayerRating.builder()
                            .id(UUID.randomUUID().toString())
                            .player(player)
                            .build()
            );
        }
    }
}
