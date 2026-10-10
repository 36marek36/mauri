package com.example.mauri.service.impl;

import com.example.mauri.enums.PlayerLevel;
import com.example.mauri.enums.Sport;
import com.example.mauri.exception.ResourceAlreadyExistsException;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.PlayerMapper;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.create.CreatePlayerDTO;
import com.example.mauri.model.dto.request.LeagueShortDTO;
import com.example.mauri.model.dto.request.PlayerShortDTO;
import com.example.mauri.model.dto.request.TeamShortDTO;
import com.example.mauri.model.dto.response.PlayerResponseDTO;
import com.example.mauri.model.dto.response.PlayerStatsDTO;
import com.example.mauri.model.dto.update.UpdatePlayerDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.PlayerRatingService;
import com.example.mauri.service.PlayerService;
import com.example.mauri.service.PlayerStatsService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final PlayerStatsService playerStatsService;
    private final PlayerRatingService playerRatingService;

    private final PlayerRatingRepository playerRatingRepository;


    @Override
    public List<PlayerResponseDTO> getAllPlayers() {
        List<Player> players = playerRepository.findAll();

        List<Player> tennisPlayers = players.stream()
                .filter(player -> player.getSports().contains(Sport.TENNIS))
                .toList();

        Map<String, Integer> ratingsByPlayerId = getRatingsByPlayerIds(tennisPlayers);

        return players.stream()
                .map(player -> {
                    PlayerResponseDTO dto = playerMapper.mapToResponseDTO(player);

                    if (player.getSports().contains(Sport.TENNIS)) {
                        setRatingAndLevel(dto, ratingsByPlayerId.get(player.getId())
                        );
                    }

                    return dto;
                })
                .toList();
    }

    @Override
    public List<PlayerResponseDTO> getActiveTennisPlayers() {
        List<Player> players = playerRepository.findByActiveTrueAndSportsContainingOrderByLastNameAsc(Sport.TENNIS);

        Map<String, Integer> ratingsByPlayerId = getRatingsByPlayerIds(players);

        return players.stream()
                .map(player -> {
                    PlayerResponseDTO dto = playerMapper.mapToResponseDTO(player);
                    setRatingAndLevel(dto, ratingsByPlayerId.get(player.getId())
                    );
                    return dto;
                })
                .toList();              // a všetky sa uložia do zoznamu
    }

    @Override
    public List<PlayerResponseDTO> getInactiveTennisPlayers() {
        List<Player> players = playerRepository.findByActiveFalseAndSportsContainingOrderByLastNameAsc(Sport.TENNIS);

        Map<String, Integer> ratingsByPlayerId = getRatingsByPlayerIds(players);

        return players.stream()
                .map(player -> {
                    PlayerResponseDTO dto = playerMapper.mapToResponseDTO(player);
                    setRatingAndLevel(dto, ratingsByPlayerId.get(player.getId())
                    );
                    return dto;
                })
                .toList();
    }

    @Override
    public List<PlayerResponseDTO> getActiveVolleyballPlayers() {
        List<Player> players = playerRepository.findByActiveTrueAndSportsContainingOrderByLastNameAsc(Sport.VOLLEYBALL);
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
    @Transactional
    public PlayerResponseDTO createPlayer(CreatePlayerDTO createPlayerDTO) {
        String firstName = ParticipantNameUtils.capitalizeNamePart(createPlayerDTO.getFirstName());
        String lastName = ParticipantNameUtils.capitalizeNamePart(createPlayerDTO.getLastName());

        boolean exists = playerRepository.existsByFirstNameAndLastName(firstName, lastName);
        if (exists) {
            throw new ResourceAlreadyExistsException("Hráč s týmto menom už existuje.");
        }

        Player player = Player.builder()
                .id(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(createPlayerDTO.getEmail())
                .phone(createPlayerDTO.getPhone())
                .sports(createPlayerDTO.getSports())
                .build();

        Player saved = playerRepository.save(player);

        boolean isTennisPlayer = saved.getSports().contains(Sport.TENNIS);

        if (isTennisPlayer) {
            playerRatingService.createRatingIfNotExists(saved);
        }

        PlayerResponseDTO responseDTO = playerMapper.mapToResponseDTO(saved);

        if (isTennisPlayer) {
            responseDTO.setRating(playerRatingService.getRating(saved.getId()));
        }
        return responseDTO;
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

        player.setDeletedDate(LocalDate.now());
        player.setActive(false);

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
            playerRatingRepository.findByPlayerId(id)
                    .ifPresent(playerRatingRepository::delete);

            playerRepository.delete(player);

            return "deleted";
        }
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

        if (updatedPlayer.getSports() != null) {

            boolean wasTennisPlayer = existingPlayer.getSports().contains(Sport.TENNIS);
            boolean willBeTennisPlayer = updatedPlayer.getSports().contains(Sport.TENNIS);

            existingPlayer.setSports(updatedPlayer.getSports());

            if (!wasTennisPlayer && willBeTennisPlayer) {
                playerRatingService.createRatingIfNotExists(existingPlayer);
            }
        }

        if (updatedPlayer.getActive() != null) {
            existingPlayer.setActive(updatedPlayer.getActive());

            if (updatedPlayer.getActive()) {
                existingPlayer.setDeletedDate(null);
            } else {
                existingPlayer.setDeletedDate(LocalDate.now());
            }
        }

        Player saved = playerRepository.save(existingPlayer);

        PlayerResponseDTO responseDTO = playerMapper.mapToResponseDTO(saved);

        if (saved.getSports().contains(Sport.TENNIS)) {
            Integer rating = playerRatingService.getRating(saved.getId());
            setRatingAndLevel(responseDTO, rating);
        }

        return responseDTO;
    }


    @Override
    public List<PlayerShortDTO> getPlayersNotInLeague(String leagueId) {
        List<Player> players = playerRepository.findPlayersNotInLeagueBySport(leagueId, Sport.TENNIS);
        return players.stream()
                .map(playerMapper::mapToPlayerShortDTO)
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

        if (player.getSports().contains(Sport.TENNIS)) {
            Integer rating = playerRatingService.getRating(player.getId());

            setRatingAndLevel(dto, rating);
        }

        dto.setTeams(teams.stream()
                .map(team -> new TeamShortDTO(team.getId(), ParticipantNameUtils.buildTeamShortName(team)))
                .toList());

        dto.setLeagues(leagues.stream()
                .map(league -> {

                    PlayerStatsDTO playerStats = playerStatsService.getAllStatsForLeague(league.getId())
                            .stream()
                            .filter(stats -> stats.getPlayerId().equals(player.getId()))
                            .findFirst()
                            .orElse(null);

                    return new LeagueShortDTO(
                            league.getId(),
                            league.getName(),
                            league.getSeason().getYear(),
                            league.getLeagueType(),
                            league.getStatus(),
                            playerStats != null ? playerStats.getRank() : null
                    );
                })
                .toList());

        return dto;
    }

    private Map<String, Integer> getRatingsByPlayerIds(List<Player> players) {
        if (players.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> playerIds = players.stream()
                .map(Player::getId)
                .toList();

        return playerRatingRepository.findByPlayerIdIn(playerIds)
                .stream()
                .collect(Collectors.toMap(
                        rating -> rating.getPlayer().getId(),
                        PlayerRating::getRating
                ));
    }

    private void setRatingAndLevel(PlayerResponseDTO dto, Integer rating) {
        dto.setRating(rating);

        if (rating != null) {
            dto.setLevel(PlayerLevel.fromRating(rating));
        }
    }

    @Transactional
    @Override
    public void addTennisToAllPlayers() {
        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            player.getSports().add(Sport.TENNIS);
        }
    }

}
