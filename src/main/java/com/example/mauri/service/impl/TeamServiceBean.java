package com.example.mauri.service.impl;

import com.example.mauri.enums.PlayerLevel;
import com.example.mauri.enums.Sport;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.TeamMapper;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.request.LeagueShortDTO;
import com.example.mauri.model.dto.request.TeamShortDTO;
import com.example.mauri.model.dto.response.PlayerResponseDTO;
import com.example.mauri.model.dto.response.TeamResponseDTO;
import com.example.mauri.model.dto.response.TeamStatsDTO;
import com.example.mauri.model.dto.update.ChangeTeamDTO;
import com.example.mauri.model.dto.update.UpdateTeamDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.TeamService;
import com.example.mauri.service.TeamStatsService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TeamServiceBean implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final LeagueRepository leagueRepository;
    private final MatchRepository matchRepository;
    private final TeamMapper teamMapper;
    private final TeamStatsService teamStatsService;

    private final PlayerRatingServiceBean playerRatingService;
    private final PlayerRatingRepository playerRatingRepository;


    @Override
    public List<TeamResponseDTO> getActiveTeams() {
        List<Team> teams = teamRepository.findByActiveTrueOrderByPlayer1LastNameAsc();

        Map<String, Integer> ratingsByPlayerId = getRatingsByPlayerIds(teams);

        return teams.stream()
                .map(team -> {
                    TeamResponseDTO dto = teamMapper.mapToResponseDTO(team);

                    if (dto.getPlayer1() != null) {
                        setRatingAndLevel(
                                dto.getPlayer1(),
                                ratingsByPlayerId.get(dto.getPlayer1().getId())
                        );
                    }

                    if (dto.getPlayer2() != null) {
                        setRatingAndLevel(
                                dto.getPlayer2(),
                                ratingsByPlayerId.get(dto.getPlayer2().getId())
                        );
                    }

                    return dto;
                })
                .toList();
    }

    @Override
    public List<TeamResponseDTO> getInactiveTeams() {
        List<Team> teams = teamRepository.findByActiveFalseOrderByPlayer1LastNameAsc();

        Map<String, Integer> ratingsByPlayerId = getRatingsByPlayerIds(teams);

        return teams.stream()
                .map(team -> {
                    TeamResponseDTO dto = teamMapper.mapToResponseDTO(team);

                    if (dto.getPlayer1() != null) {
                        setRatingAndLevel(
                                dto.getPlayer1(),
                                ratingsByPlayerId.get(dto.getPlayer1().getId())
                        );
                    }

                    if (dto.getPlayer2() != null) {
                        setRatingAndLevel(
                                dto.getPlayer2(),
                                ratingsByPlayerId.get(dto.getPlayer2().getId())
                        );
                    }

                    return dto;
                })
                .toList();
    }

    @Override
    public Team getTeamById(@NonNull String id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));
    }

    @Override
    public TeamResponseDTO getTeamResponseById(String id) {
        // 1. Skontroluj, či je používateľ prihlásený
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Používateľ neexistuje")); // 404

        // 2. Načítaj tím
        Team team = getTeamById(id);

        // 3. Overenie práv
        // Ak používateľ nemá showDetails, môže vidieť len tímy, ktorých je členom
        if (!user.isShowDetails()) {
            Player myPlayer = user.getPlayer();

            if (myPlayer == null) {
                throw new AccessDeniedException("Nemáte povolenie zobraziť detail tímu."); // 403
            }

            // Over, či je používateľ hráčom v tíme
            boolean isMember = myPlayer.getId().equals(team.getPlayer1().getId())
                    || myPlayer.getId().equals(team.getPlayer2().getId());

            if (!isMember) {
                throw new AccessDeniedException("Nemáte povolenie zobraziť detail tímu."); // 403
            }
        }

        // 4. Vráť DTO
        return mapFullTeam(team);
    }

    @Override
    public TeamResponseDTO createTeam(String player1Id, String player2Id) {

        if (player1Id.equals(player2Id)) {
            throw new IllegalArgumentException("Hráč nemôže byť v tíme sám so sebou.");
        }

        Player player1 = playerRepository.findById(player1Id)
                .orElseThrow(() -> new ResourceNotFoundException("No player found with id: " + player1Id));

        Player player2 = playerRepository.findById(player2Id)
                .orElseThrow(() -> new ResourceNotFoundException("No player found with id: " + player2Id));

        boolean exists = teamRepository.existsByPlayers(player1Id, player2Id);

        if (exists) {
            throw new IllegalStateException("Tím už existuje.");
        }

        Team team = Team.builder()
                .id(UUID.randomUUID().toString())
                .player1(player1)
                .player2(player2)
                .build();

        Team saved = teamRepository.save(team);

        return teamMapper.mapToResponseDTO(saved);
    }

    @Override
    public String deleteTeam(String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));

        boolean isInLeagues = !leagueRepository.findLeaguesByTeamId(id).isEmpty();
        boolean isInMatch = matchRepository.existsByHomeTeamIdOrAwayTeamId(id, id);

        if (isInLeagues || isInMatch) {
            deactivateTeam(id);
            return "deactivated";
        } else {
            teamRepository.delete(team);
            return "deleted";
        }
    }

    @Override
    public List<TeamResponseDTO> getActiveTeamsNotInAnyActiveLeague() {
        List<Team> freeTeams = teamRepository.findActiveTeamsWithoutActiveLeague();
        return freeTeams.stream()
                .map(teamMapper::mapToResponseDTO)
                .toList();
    }

    @Override
    public void deactivateTeam(@NonNull String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));
        team.setDeletedDate(LocalDate.now());
        team.setActive(false);
        teamRepository.save(team);
    }

    @Override
    public void deactivateTeamsWithPlayer(@NonNull String playerId) {
        List<Team> teams = teamRepository.findByPlayer1IdOrPlayer2Id(playerId, playerId);
        for (Team team : teams) {
            deactivateTeam(team.getId());
        }
    }

    @Override
    public List<TeamShortDTO> getTeamsNotInLeague(String leagueId) {
        List<Team> teams = teamRepository.findTeamsNotInLeague(leagueId);
        return teams.stream()
                .map(teamMapper::mapToTeamShortDTO)
                .toList();
    }

    @Override
    @Transactional
    public void changePlayerInTeam(String teamId, ChangeTeamDTO changeTeamDTO) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + teamId));

        if (team.getPlayer1().getId().equals(changeTeamDTO.getNewPlayerId())
                || team.getPlayer2().getId().equals(changeTeamDTO.getNewPlayerId())) {
            throw new IllegalArgumentException("Player is already in the team.");
        }
        Player newPlayer = playerRepository.findById(changeTeamDTO.getNewPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("No player found with id: " + changeTeamDTO.getNewPlayerId()));

        if (team.getPlayer1().getId().equals(changeTeamDTO.getOldPlayerId())) {
            team.setPlayer1(newPlayer);
        } else if (team.getPlayer2().getId().equals(changeTeamDTO.getOldPlayerId())) {
            team.setPlayer2(newPlayer);
        } else {
            throw new IllegalArgumentException("Player not found in team.");
        }
    }

    @Override
    public TeamResponseDTO updateTeam(String teamId, UpdateTeamDTO updatedTeam) {
        Team existingTeam = getTeamOrThrow(teamId);
        if (updatedTeam.getActive() != null) {
            existingTeam.setActive(updatedTeam.getActive());

            if (updatedTeam.getActive()) {
                existingTeam.setDeletedDate(null);
            } else {
                existingTeam.setDeletedDate(LocalDate.now());
            }
        }
        Team updated = teamRepository.save(existingTeam);
        return teamMapper.mapToResponseDTO(updated);
    }

    private Team getTeamOrThrow(String id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    }

    private Map<String, Integer> getRatingsByPlayerIds(List<Team> teams) {
        List<String> playerIds = teams.stream()
                .flatMap(team -> Stream.of(team.getPlayer1(), team.getPlayer2()))
                .filter(Objects::nonNull)
                .filter(player -> player.getSports().contains(Sport.TENNIS))
                .map(Player::getId)
                .distinct()
                .toList();

        if (playerIds.isEmpty()) {
            return Collections.emptyMap();
        }

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

    private TeamResponseDTO mapFullTeam(Team team) {
        List<League> leagues = leagueRepository.findLeaguesByTeamId(team.getId());

        TeamResponseDTO teamResponseDTO = teamMapper.mapToResponseDTO(team);

        if (teamResponseDTO.getPlayer1() != null
                && team.getPlayer1().getSports().contains(Sport.TENNIS)) {

            Integer rating = playerRatingService.getRating(team.getPlayer1().getId());
            setRatingAndLevel(teamResponseDTO.getPlayer1(), rating);
        }

        if (teamResponseDTO.getPlayer2() != null
                && team.getPlayer2().getSports().contains(Sport.TENNIS)) {

            Integer rating = playerRatingService.getRating(team.getPlayer2().getId());
            setRatingAndLevel(teamResponseDTO.getPlayer2(), rating);
        }

        teamResponseDTO.setLeagues(leagues.stream()
                .map(league -> {
                    TeamStatsDTO teamStats = teamStatsService.getAllStatsForLeague(league.getId())
                            .stream()
                            .filter(stats -> stats.getTeamId().equals(team.getId()))
                            .findFirst()
                            .orElse(null);

                    return new LeagueShortDTO(
                            league.getId(),
                            league.getName(),
                            league.getSeason().getYear(),
                            league.getLeagueType(),
                            league.getStatus(),
                            teamStats != null ? teamStats.getRank() : null
                    );
                })
                .toList());

        return teamResponseDTO;
    }
}
