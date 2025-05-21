package com.example.mauri.service;

import com.example.mauri.model.Player;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.List;

public interface PlayerService {
    List<Player> getPlayers();

    Player getPlayer(@NonNull String id);

    Player addPlayer(String firstName, String lastName, String email, String phone, LocalDate registrationDate);

    void deletePlayer(@NonNull String id);

    List<Player> getPlayersNotInAnyLeague();

}
