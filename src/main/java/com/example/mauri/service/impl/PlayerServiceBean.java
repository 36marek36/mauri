package com.example.mauri.service.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.PlayerMapper;
import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.create.CreatePlayerDTO;
import com.example.mauri.model.dto.response.PlayerResponseDTO;
import com.example.mauri.model.dto.update.UpdatePlayerDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.PlayerService;
import com.example.mauri.service.TeamService;
import jakarta.transaction.Transactional;
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
    private final TeamService teamService;
    private final PlayerMapper playerMapper;


    @Override
    public List<PlayerResponseDTO> getActivePlayers() {
        List<Player> players = playerRepository.findByActiveTrue();

        return players.stream()
                .map(playerMapper::mapToResponseDTO) // každý Player sa zmení na PlayerResponseDTO
                .toList();                   // a všetky sa uložia do zoznamu
    }

    @Override
    public List<PlayerResponseDTO> getInactivePlayers() {
        List<Player> players = playerRepository.findByActiveFalse();
        return players.stream()
                .map(playerMapper::mapToResponseDTO)
                .toList();
    }

    @Override
    public Player getPlayer(@NonNull String id) {
        return getPlayerOrThrow(id);
    }

    @Override
    public PlayerResponseDTO getPlayerResponseById(String id) {
        Player player = getPlayer(id);
        return playerMapper.mapToResponseDTO(player);
    }

    @Override
    public PlayerResponseDTO createPlayer(CreatePlayerDTO createPlayerDTO) {
        Player player = Player.builder()
                .id(UUID.randomUUID().toString())
                .firstName(createPlayerDTO.getFirstName())
                .lastName(createPlayerDTO.getLastName())
                .email(createPlayerDTO.getEmail())
                .phone(createPlayerDTO.getPhone())
                .build();
        Player saved = playerRepository.save(player);
        return playerMapper.mapToResponseDTO(saved);
    }

    @Override
    public void assignPlayerToUser(String playerId, String userId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getPlayer() != null) {
            throw new IllegalStateException("Používatel " + user.getUsername() + " už má hráča priradeného!");
        }

        user.setPlayer(player);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public PlayerResponseDTO createAndAssignPlayerToUser(CreatePlayerDTO createPlayerDTO, String userId) {
        PlayerResponseDTO created = createPlayer(createPlayerDTO);
        assignPlayerToUser(created.getId(), userId);
        return created;
    }

    @Transactional
    @Override
    public String deletePlayer(@NonNull String id) {
        Player player = getPlayerOrThrow(id);

        detachPlayerFromUsers(player.getId());

        boolean isInTeam = teamRepository.existsByPlayer1IdOrPlayer2Id(id, id);
        boolean isInLeague = !leagueRepository.findLeaguesByPlayerId(id).isEmpty();
        boolean isInMatch = matchRepository.existsByHomePlayerIdOrAwayPlayerId(id, id);

        if (isInTeam || isInLeague || isInMatch) {
            deactivatePlayer(id);
            if (isInTeam) {
                teamService.deactivateTeamsWithPlayer(id);
                return "deactivated_player_in_team";
            }
            return "deactivated";
        } else {
            playerRepository.delete(player);
            return "deleted";
        }
    }

    @Override
    public List<PlayerResponseDTO> getActivePlayersNotInAnyActiveLeague() {
        List<Player> freePlayers = playerRepository.findActivePlayersWithoutActiveLeague();

        return freePlayers.stream()
                .map(playerMapper::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<PlayerResponseDTO> getPlayersWithoutUser() {
        List<Player> players = playerRepository.findActivePlayersWithoutUser();
        return players.stream()
                .map(playerMapper::mapToResponseDTO)
                .toList();
    }

    @Transactional
    @Override
    public void deactivatePlayer(String playerId) {
        Player player = getPlayerOrThrow(playerId);

        detachPlayerFromUsers(playerId);
        player.setDeletedDate(LocalDate.now());

        player.setActive(false);
        playerRepository.save(player);
    }

    @Override
    public PlayerResponseDTO updatePlayer(String playerId, UpdatePlayerDTO updatedPlayer) {
        Player existingPlayer = getPlayerOrThrow(playerId);

        if (updatedPlayer.getFirstName() != null) {
            existingPlayer.setFirstName(updatedPlayer.getFirstName());
        }
        if (updatedPlayer.getLastName() != null) {
            existingPlayer.setLastName(updatedPlayer.getLastName());
        }
        if (updatedPlayer.getEmail() != null) {
            existingPlayer.setEmail(updatedPlayer.getEmail());
        }
        if (updatedPlayer.getPhone() != null) {
            existingPlayer.setPhone(updatedPlayer.getPhone());
        }
        Player saved = playerRepository.save(existingPlayer);
        return playerMapper.mapToResponseDTO(saved);
    }

    private Player getPlayerOrThrow(String id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
    }

    private void detachPlayerFromUsers(String playerId) {
        List<User> users = userRepository.findByPlayerId(playerId);
        for (User user : users) {
            user.setPlayer(null);
            userRepository.save(user);
        }
    }

}
