package com.example.mauri.service.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.League;
import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.CreatePlayerDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.PlayerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerServiceBean implements PlayerService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final MatchRepository matchRepository;


    @Override
    public List<Player> getActivePlayers() {
        return playerRepository.findByActiveTrue();
    }

    @Override
    public List<Player> getInactivePlayers() {
        return playerRepository.findByActiveFalse();
    }

    @Override
    public Player getPlayer(@NonNull String id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No player found with id: " + id));
    }

    @Override
    public Player createPlayer(CreatePlayerDTO createPlayerDTO) {
        var player = new Player(UUID.randomUUID().toString(), createPlayerDTO.getFirstName(), createPlayerDTO.getLastName(), createPlayerDTO.getEmail(), createPlayerDTO.getPhone(), LocalDate.now(),null, true);
        playerRepository.save(player);
        return player;
    }

    @Override
    public void deletePlayer(@NonNull String id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        detachPlayerFromUsers(player.getId());

        boolean isInTeam = teamRepository.existsByPlayer1IdOrPlayer2Id(id, id);

        List<League> leagues = leagueRepository.findLeaguesByPlayerId(id);
        boolean isInLeague = !leagues.isEmpty();

        boolean isInMatch = matchRepository.existsByHomePlayerIdOrAwayPlayerId(id, id);

        if (isInTeam || isInLeague || isInMatch) {
            deactivatePlayer(id);
        } else {
            playerRepository.delete(player);
        }
    }

    @Override
    public List<Player> getActivePlayersNotInAnyActiveLeague() {
        return playerRepository.findActivePlayersWithoutActiveLeague();
    }

    @Override
    public List<Player> getPlayersWithoutUser() {
        return playerRepository.findActivePlayersWithoutUser();
    }

    @Override
    public void deactivatePlayer(String playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        detachPlayerFromUsers(playerId);
        player.setDeletedDate(LocalDate.now());

        player.setActive(false);
        playerRepository.save(player);
    }

    private void detachPlayerFromUsers(String playerId) {
        List<User> users = userRepository.findByPlayerId(playerId);
        for (User user : users) {
            user.setPlayer(null);
            userRepository.save(user);
        }
    }
}
