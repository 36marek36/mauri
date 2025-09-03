package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.create.CreateMatchDTO;
import com.example.mauri.model.dto.request.ParticipantDTO;
import com.example.mauri.model.dto.response.MatchResponseDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.*;
import com.example.mauri.util.ParticipantNameUtils;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceBean implements MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final LeagueRepository leagueRepository;
    private final RoundRobinPlayersService roundRobinPlayersService;
    private final RoundRobinTeamsService roundRobinTeamsService;
    private final SeasonRepository seasonRepository;
    private final MatchResultService matchResultService;

    @Override
    public List<MatchResponseDTO> getMatches() {
        List<Match> matches = matchRepository.findAll();
        return matches.stream()
                .map(this::mapMatchToDTO)
                .toList();
    }

    @Override
    public MatchResponseDTO getMatch(@NonNull String id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No Match found with id: " + id));
        return mapMatchToDTO(match);
    }

    @Override
    public MatchResponseDTO createMatch(CreateMatchDTO createMatchDTO) {
        Match match = Match.builder()
                .id(UUID.randomUUID().toString())
                .matchType(createMatchDTO.getMatchType())
                .leagueId(createMatchDTO.getLeagueId())
                .build();

        switch (createMatchDTO.getMatchType()) {
            case SINGLES -> {
                match.setHomePlayer(playerRepository.findById(createMatchDTO.getPlayer1Id())
                        .orElseThrow(() -> new ResourceNotFoundException("No Player found with id: " + createMatchDTO.getPlayer1Id())));
                match.setAwayPlayer(playerRepository.findById(createMatchDTO.getPlayer2Id())
                        .orElseThrow(() -> new ResourceNotFoundException("No Player found with id: " + createMatchDTO.getPlayer2Id())));
            }
            case DOUBLES -> {
                match.setHomeTeam(teamRepository.findById(createMatchDTO.getTeam1Id())
                        .orElseThrow(() -> new ResourceNotFoundException("No Team found with id: " + createMatchDTO.getTeam1Id())));
                match.setAwayTeam(teamRepository.findById(createMatchDTO.getTeam2Id())
                        .orElseThrow(() -> new ResourceNotFoundException("No Team found with id: " + createMatchDTO.getTeam2Id())));
            }
            default -> throw new IllegalArgumentException("Unsupported MatchType: " + createMatchDTO.getMatchType());
        }
        match = matchRepository.save(match);
        return mapMatchToDTO(match);
    }

    @Override
    public void deleteMatch(@NonNull String id) {
        if (!matchRepository.existsById(id)) {
            throw new ResourceNotFoundException("No Match found with id: " + id);
        }
        matchRepository.deleteById(id);
    }

    @Override
    public Match addResult(String matchId, MatchResult matchResult) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("No Match found with id: " + matchId));

        MatchResult finalResult = matchResultService.processResult(match, matchResult);
        match.setResult(finalResult);
        match.setStatus(MatchStatus.FINISHED);
        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public List<MatchResponseDTO> generateMatchesForLeague(String leagueId) {
        log.info("Začiatok generovania zápasov pre ligu s ID: {}", leagueId);

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("No League found with id: " + leagueId));

        if (matchRepository.existsByLeagueId(leagueId)) {
            throw new IllegalStateException("Zápasy pre ligu '" + league.getName() + "' už existujú!");
        }

        MatchType type = league.getLeagueType();
        List<Match> matches;

        switch (type) {
            case SINGLES -> {
                List<Player> players = league.getPlayers();
                if (players.size() < 2) {
                    throw new IllegalStateException("Liga '" + league.getName() + "' musí obsahovať aspoň 2 hráčov.");
                }
                matches = roundRobinPlayersService.generateMatches(new ArrayList<>(players), leagueId, type);
            }
            case DOUBLES -> {
                List<Team> teams = league.getTeams();
                if (teams.size() < 2) {
                    throw new IllegalStateException("Liga '" + league.getName() + "' musí obsahovať aspoň 2 tímy.");
                }
                matches = roundRobinTeamsService.generateMatches(new ArrayList<>(teams), leagueId, type);
            }
            default -> throw new UnsupportedOperationException("Nepodporovaný typ zápasu: " + type);
        }

        matchRepository.saveAll(matches);

        league.setStatus(LeagueStatus.ACTIVE);
        leagueRepository.save(league);

        log.info("Úspešne vygenerovaných {} zápasov pre ligu '{}'", matches.size(), league.getName());

        return matches.stream()
                .map(this::mapMatchToDTO)
                .toList();
    }

    @Override
    public Map<Integer, List<MatchResponseDTO>> getMatchesGroupedByRound(String leagueId) {
        List<Match> matches = matchRepository.findByLeagueId(leagueId);

        return matches.stream()
                .map(this::mapMatchToDTO) // najprv mapuješ na DTO
                .collect(Collectors.groupingBy(MatchResponseDTO::getRoundNumber)); // potom group-by
    }

    @Override
    public int getTotalMatchesCount(String leagueId) {
        return matchRepository.countByLeagueId(leagueId);
    }

    @Override
    public int getPlayedMatchesCount(String leagueId) {
        return matchRepository.countPlayedMatches(leagueId);
    }

    @Transactional
    @Override
    public void cancelResult(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("No Match found with id: " + matchId));

        match.setStatus(MatchStatus.CREATED);
        match.setResult(null);
        matchRepository.save(match);
    }

    @Override
    public List<MatchResponseDTO> getMatchesForPlayerInActiveSeason(String playerId, MatchStatus status) {
        List<String> leagueIds = getActiveSeasonLeagueIds();
        if (leagueIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Match> matches = matchRepository.findByPlayerStatusAndLeagueIds(playerId, status, leagueIds);
        return matches.stream()
                .map(this::mapMatchToDTO)
                .toList();
    }

    @Override
    public List<MatchResponseDTO> getMatchesForTeamInActiveSeason(String teamId, MatchStatus status) {
        List<String> leagueIds = getActiveSeasonLeagueIds();
        if (leagueIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Match> matches = matchRepository.findByTeamStatusAndLeagueIds(teamId, status, leagueIds);
        return matches.stream()
                .map(this::mapMatchToDTO)
                .toList();
    }

    private List<String> getActiveSeasonLeagueIds() {
        Season activeSeason = seasonRepository.findByStatus(SeasonStatus.ACTIVE).orElse(null);

        if (activeSeason == null) {
            return new ArrayList<>();
        }

        List<String> leagueIds = new ArrayList<>();
        for (League league : activeSeason.getLeagues()) {
            leagueIds.add(league.getId());
        }
        return leagueIds;
    }

    private MatchResponseDTO mapMatchToDTO(Match match) {
        ParticipantDTO homePlayer = null;
        ParticipantDTO awayPlayer = null;
        ParticipantDTO homeTeam = null;
        ParticipantDTO awayTeam = null;
        switch (match.getMatchType()) {
            case SINGLES -> {
                if (match.getHomePlayer() != null) {
                    String name = ParticipantNameUtils.buildPlayerName(match.getHomePlayer());
                    homePlayer = new ParticipantDTO(match.getHomePlayer().getId(), name,match.getHomePlayer().isActive());
                }
                if (match.getAwayPlayer() != null) {
                    String name = ParticipantNameUtils.buildPlayerName(match.getAwayPlayer());
                    awayPlayer = new ParticipantDTO(match.getAwayPlayer().getId(), name,match.getAwayPlayer().isActive());
                }
            }
            case DOUBLES -> {
                if (match.getHomeTeam() != null) {
                    String teamName = ParticipantNameUtils.buildTeamName(match.getHomeTeam());
                    homeTeam = new ParticipantDTO(match.getHomeTeam().getId(), teamName,match.getHomeTeam().isActive());
                }
                if (match.getAwayTeam() != null) {
                    String teamName = ParticipantNameUtils.buildTeamName(match.getAwayTeam());
                    awayTeam = new ParticipantDTO(match.getAwayTeam().getId(), teamName,match.getAwayTeam().isActive());
                }
            }
        }
        return MatchResponseDTO.builder()
                .id(match.getId())
                .matchType(match.getMatchType())
                .leagueId(match.getLeagueId())
                .result(match.getResult())
                .roundNumber(match.getRoundNumber())
                .status(match.getStatus())
                .homePlayer(homePlayer)
                .awayPlayer(awayPlayer)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .build();
    }
}
