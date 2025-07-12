package com.example.mauri.service;

import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.CreatePlayerDTO;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.UserRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PlayerServiceBean implements PlayerService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public PlayerServiceBean(PlayerRepository playerRepository, UserRepository userRepository) {
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
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
        List<User> usersWithPlayer = userRepository.findByPlayerId(id);

        for (User user : usersWithPlayer) {
            user.setPlayer(null);
        }
        userRepository.saveAll(usersWithPlayer);

        playerRepository.deleteById(id);
    }

    @Override
    public List<Player> getPlayersNotInAnyActiveLeague() {
        return playerRepository.findPlayersNotInAnyActiveLeague();
    }

}
