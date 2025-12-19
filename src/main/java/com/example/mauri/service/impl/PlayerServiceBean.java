package com.example.mauri.service.impl;

import com.example.mauri.exception.PlayerAlreadyExistsException;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.PlayerMapper;
import com.example.mauri.model.League;
import com.example.mauri.model.Player;
import com.example.mauri.model.Team;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.create.CreatePlayerDTO;
import com.example.mauri.model.dto.request.LeagueShortDTO;
import com.example.mauri.model.dto.request.TeamShortDTO;
import com.example.mauri.model.dto.response.PlayerResponseDTO;
import com.example.mauri.model.dto.update.UpdatePlayerDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.PlayerService;
import com.example.mauri.service.TeamService;
import com.example.mauri.util.ParticipantNameUtils;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
                .map(this::mapFullPlayer) // každý Player sa zmení na PlayerResponseDTO
                .toList();                   // a všetky sa uložia do zoznamu
    }

    @Override
    public List<PlayerResponseDTO> getInactivePlayers() {
        List<Player> players = playerRepository.findByActiveFalse();
        return players.stream()
                .map(this::mapFullPlayer)
                .toList();
    }

    @Override
    public Player getPlayer(@NonNull String id) {
        return getPlayerOrThrow(id);
    }

    @Override
    public PlayerResponseDTO getPlayerResponseById(String id) {

        // 1. Skontroluj, či je používateľ prihlásený
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Používateľ neexistuje")); // 404

        // 2. Načítaj hráča
        Player player = getPlayerOrThrow(id);

        // 3. Overenie práv
        // Ak používateľ nemá showDetails, môže vidieť iba svojho hráča
        if (!user.isShowDetails()) {
            Player myPlayer = user.getPlayer();

            if (myPlayer == null || !myPlayer.getId().equals(player.getId())) {
                throw new AccessDeniedException("Nemáte povolenie zobraziť detail tohto hráča.");
            }
        }

        // 4. Vráť DTO
        return mapFullPlayer(player);
    }

    @Override
    public PlayerResponseDTO createPlayer(CreatePlayerDTO createPlayerDTO) {
        String firstName = ParticipantNameUtils.capitalizeNamePart(createPlayerDTO.getFirstName());
        String lastName = ParticipantNameUtils.capitalizeNamePart(createPlayerDTO.getLastName());

        boolean exists = playerRepository.existsByFirstNameAndLastName(createPlayerDTO.getFirstName(), createPlayerDTO.getLastName());
        if (exists) {
            throw new PlayerAlreadyExistsException("Hráč s týmto menom už existuje.");
        }

        Player player = Player.builder()
                .id(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
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
                .map(this::mapFullPlayer)
                .toList();
    }

    @Override
    public List<PlayerResponseDTO> getPlayersWithoutUser() {
        List<Player> players = playerRepository.findActivePlayersWithoutUser();
        return players.stream()
                .map(this::mapFullPlayer)
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


    @Override
    public List<PlayerResponseDTO> getPlayersNotInLeague(String leagueId) {
        List<Player> players = playerRepository.findPlayersNotInLeague(leagueId);
        return players.stream()
                .map(this::mapFullPlayer)
                .toList();
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

    private PlayerResponseDTO mapFullPlayer(Player player) {
        List<Team> teams = teamRepository.findByPlayer1IdOrPlayer2Id(player.getId(), player.getId());
        List<League> leagues = leagueRepository.findLeaguesByPlayerId(player.getId());

        PlayerResponseDTO dto = playerMapper.mapToResponseDTO(player);

        dto.setTeams(teams.stream()
                .map(team -> new TeamShortDTO(team.getId(), ParticipantNameUtils.buildTeamName(team)))
                .toList());

        dto.setLeagues(leagues.stream()
                .map(league -> new LeagueShortDTO(league.getId(), league.getName(), league.getSeason().getYear(), league.getLeagueType(), league.getStatus()))
                .toList());

        return dto;
    }

}
