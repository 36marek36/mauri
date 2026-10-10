package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.MatchMapper;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.create.CreateMatchDTO;
import com.example.mauri.model.dto.response.MatchResponseDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.*;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final MatchMapper matchMapper;
    private final MatchActivityService matchActivityService;
    private final MatchActivityRepository matchActivityRepository;
    private final PlayerRatingRepository playerRatingRepository;
    private final PlayerRatingService playerRatingService;

    @Override
    public List<MatchResponseDTO> getMatches() {
        List<Match> matches = matchRepository.findAll();
        return matches.stream()
                .map(matchMapper::mapMatchToDTO)
                .toList();
    }

    @Override
    public MatchResponseDTO getMatch(@NonNull String id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No Match found with id: " + id));
        return matchMapper.mapMatchToDTO(match);
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
        return matchMapper.mapMatchToDTO(match);
    }

    @Override
    public void deleteMatch(@NonNull String id) {
        if (!matchRepository.existsById(id)) {
            throw new ResourceNotFoundException("No Match found with id: " + id);
        }
        matchRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Match addResult(String matchId, MatchResult matchResult) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("No Match found with id: " + matchId));

        League league = leagueRepository.findById(match.getLeagueId()).orElseThrow();
        Season season = league.getSeason();

        if (season.getStatus() == SeasonStatus.FINISHED) {
            throw new IllegalStateException("Sezóna je ukončená, úpravy nie sú povolené.");
        }

        boolean ratingWasCalculated = match.isRatingCalculated();

        // Ak už bol rating vypočítaný,
        // vrátime hráčov na stav pred týmto zápasom.
        if (ratingWasCalculated) {
            restoreRatingsBeforeMatch(match);
            match.setRatingCalculated(false);
        }

        MatchResult finalResult = matchResultService.processResult(match, matchResult);
        match.setResult(finalResult);

        if (finalResult.getScratchedId() != null) {
            match.setStatus(MatchStatus.SCRATCHED);
        } else {
            match.setStatus(MatchStatus.FINISHED);
        }

        Match savedMatch = matchRepository.save(match);

        // Rating vypočítame iba pre dokončený singles zápas.
        if (savedMatch.getStatus() == MatchStatus.FINISHED && savedMatch.getMatchType() == MatchType.SINGLES) {
            playerRatingService.updateRatingsAfterMatch(savedMatch.getId());
        }

        matchActivityService.createActivity(savedMatch.getId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("{} added/updated result for match '{}', status={}", username, savedMatch.getId(), savedMatch.getStatus());

        return savedMatch;
    }

    @Override
    @Transactional
    public List<MatchResponseDTO> generateMatchesForLeague(String leagueId) {
//        log.info("Začiatok generovania zápasov pre ligu s ID: {}", leagueId);

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
                .map(matchMapper::mapMatchToDTO)
                .toList();
    }

    @Override
    public Map<Integer, List<MatchResponseDTO>> getMatchesGroupedByRound(String leagueId) {
        List<Match> matches = matchRepository.findByLeagueId(leagueId);

        return matches.stream()
                .map(matchMapper::mapMatchToDTO) // najprv mapuješ na DTO
                .collect(Collectors.groupingBy(MatchResponseDTO::getRoundNumber)); // potom group-by
    }

    @Transactional
    @Override
    public void cancelResult(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("No Match found with id: " + matchId));

        // Ak bol rating už vypočítaný,
        // vrátime hráčov na rating pred zápasom.
        if (match.isRatingCalculated()) {
            restoreRatingsBeforeMatch(match);
        }
        match.setStatus(MatchStatus.CREATED);
        match.setResult(null);
        match.setRatingCalculated(false);

        // Ratingy pred zápasom už nemajú význam,
        // pretože zápas momentálne nemá výsledok.
        match.setHomePlayerRatingBefore(null);
        match.setAwayPlayerRatingBefore(null);

        matchRepository.save(match);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        log.info("{} cancelled result for match '{}'", username, match.getId());

        matchActivityRepository.deleteByMatchId(matchId);
    }

    @Override
    public List<MatchResponseDTO> getMatchesForPlayerInActiveSeason(String playerId, MatchStatus status) {
        List<String> leagueIds = getActiveSeasonLeagueIds();
        if (leagueIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Match> matches = matchRepository.findByPlayerStatusAndLeagueIds(playerId, status, leagueIds);
        return matches.stream()
                .map(matchMapper::mapMatchToDTO)
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
                .map(matchMapper::mapMatchToDTO)
                .toList();
    }

    @Transactional
    @Override
    public void recalculateLeague(String leagueId) {
        List<Match> matches = matchRepository.findByLeagueId(leagueId);
        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.FINISHED) {
                continue;
            }
            MatchResult result = match.getResult();
            if (result == null) {
                continue;
            }
            matchResultService.recalculate(match, result);
        }
        matchRepository.saveAll(matches);
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

    private void restoreRatingsBeforeMatch(Match match) {

        if (match.getHomePlayerRatingBefore() == null || match.getAwayPlayerRatingBefore() == null) {
            throw new IllegalStateException("Zápas nemá uložené ratingy hráčov pred zápasom.");
        }

        Player homePlayer = match.getHomePlayer();
        Player awayPlayer = match.getAwayPlayer();

        if (homePlayer == null || awayPlayer == null) {
            throw new IllegalStateException("Zápas nemá oboch hráčov.");
        }

        PlayerRating homePlayerRating = playerRatingRepository
                .findByPlayerId(homePlayer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rating hráča neexistuje: " + homePlayer.getId()));

        PlayerRating awayPlayerRating = playerRatingRepository
                .findByPlayerId(awayPlayer.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Rating hráča neexistuje: " + awayPlayer.getId()));

        homePlayerRating.setRating(match.getHomePlayerRatingBefore());
        homePlayerRating.setRatingChange(0);

        awayPlayerRating.setRating(match.getAwayPlayerRatingBefore());
        awayPlayerRating.setRatingChange(0);
    }

}
