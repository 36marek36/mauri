package com.example.mauri.service;

import com.example.mauri.model.Player;
import com.example.mauri.model.dto.create.CreatePlayerDTO;
import com.example.mauri.model.dto.response.PlayerResponseDTO;
import com.example.mauri.model.dto.update.UpdatePlayerDTO;
import lombok.NonNull;

import java.util.List;

public interface PlayerService {

    List<PlayerResponseDTO> getActivePlayers();

    List<PlayerResponseDTO> getInactivePlayers();

    Player getPlayer(@NonNull String id);

    PlayerResponseDTO getPlayerResponseById(String id);

    PlayerResponseDTO createPlayer(CreatePlayerDTO createPlayerDTO);

    String deletePlayer(@NonNull String id);

    List<PlayerResponseDTO> getActivePlayersNotInAnyActiveLeague();

    List<PlayerResponseDTO> getPlayersWithoutUser();

    void deactivatePlayer(String playerId);

    PlayerResponseDTO updatePlayer(String playerId, UpdatePlayerDTO updatedPlayer);

}
