package com.example.mauri.service;

import com.example.mauri.model.Player;
import com.example.mauri.model.dto.CreatePlayerDTO;
import com.example.mauri.repository.PlayerRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PlayerServiceBean implements PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerServiceBean(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getPlayers() {
        return playerRepository.findAll().stream().toList();
    }

    @Override
    public Player getPlayer(@NonNull String id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No player found with id: " + id));
    }

    @Override
    public Player createPlayer(CreatePlayerDTO createPlayerDTO) {
        var player = new Player(UUID.randomUUID().toString(), createPlayerDTO.getFirstName(), createPlayerDTO.getLastName(), createPlayerDTO.getEmail(), createPlayerDTO.getPhone(), LocalDate.now());
        playerRepository.save(player);
        return player;
    }

    @Override
    public void deletePlayer(@NonNull String id) {
        if (!playerRepository.existsById(id)) {
            throw new IllegalArgumentException("No player found with id: " + id);
        }
        playerRepository.deleteById(id);
    }

    @Override
    public List<Player> getPlayersNotInAnyLeague() {
        return playerRepository.findPlayersNotInAnyLeague();
    }

}
