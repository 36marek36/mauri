package com.example.mauri.service;

import com.example.mauri.model.Player;
import com.example.mauri.model.dto.CreatePlayerDTO;
import lombok.NonNull;

import java.util.List;

public interface PlayerService {
    List<Player> getActivePlayers();

    List<Player> getInactivePlayers();

    Player getPlayer(@NonNull String id);

    Player createPlayer(CreatePlayerDTO createPlayerDTO);

    void deletePlayer(@NonNull String id);

    List<Player> getPlayersNotInAnyActiveLeague();

    List<Player> getPlayersWithoutUser();

    void deactivatePlayer(String playerId);

}
