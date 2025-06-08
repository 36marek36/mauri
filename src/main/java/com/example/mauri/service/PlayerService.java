package com.example.mauri.service;

import com.example.mauri.model.Player;
import com.example.mauri.model.dto.CreatePlayerDTO;
import lombok.NonNull;

import java.util.List;

public interface PlayerService {
    List<Player> getPlayers();

    Player getPlayer(@NonNull String id);

    Player createPlayer(CreatePlayerDTO createPlayerDTO);

    void deletePlayer(@NonNull String id);

    List<Player> getPlayersNotInAnyLeague();

}
