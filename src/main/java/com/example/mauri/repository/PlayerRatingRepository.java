package com.example.mauri.repository;

import com.example.mauri.model.PlayerRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRatingRepository extends JpaRepository<PlayerRating, String> {
    Optional<PlayerRating> findByPlayerId(String playerId);
    List<PlayerRating> findByPlayerIdIn(List<String> playerIds);
}
