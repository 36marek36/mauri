package com.example.mauri.service;

import com.example.mauri.model.Player;
import com.example.mauri.model.dto.create.CreatePlayerDTO;
import com.example.mauri.model.dto.update.UpdatePlayerDTO;
import lombok.NonNull;

import java.util.List;

public interface PlayerService {
    List<Player> getActivePlayers();

    List<Player> getInactivePlayers();

    Player getPlayer(@NonNull String id);

    Player createPlayer(CreatePlayerDTO createPlayerDTO);

    String deletePlayer(@NonNull String id);

    List<Player> getActivePlayersNotInAnyActiveLeague();

    List<Player> getPlayersWithoutUser();

    void deactivatePlayer(String playerId);

    Player updatePlayer(String playerId, UpdatePlayerDTO updatedPlayer);

}
