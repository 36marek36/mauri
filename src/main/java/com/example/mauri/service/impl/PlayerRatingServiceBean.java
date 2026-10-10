package com.example.mauri.service.impl;

import com.example.mauri.enums.*;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.response.PlayerRatingResponseDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.repository.PlayerRatingRepository;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.service.PlayerRatingService;
import com.example.mauri.util.ParticipantNameUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerRatingServiceBean implements PlayerRatingService {
    private final PlayerRatingRepository playerRatingRepository;
    private final LeagueRepository leagueRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private static final double K_FACTOR = 32.0;
    private static final double RATING_SCALE = 400.0;

    @Override
    public Integer getRating(String playerId) {
        return playerRatingRepository.findByPlayerId(playerId)
                .map(PlayerRating::getRating)
                .orElse(null);
    }

    @Override
    public void createRatingIfNotExists(Player player) {
        if (playerRatingRepository.findByPlayerId(player.getId()).isEmpty()) {
            playerRatingRepository.save(PlayerRating.builder()
                    .id(UUID.randomUUID().toString())
                    .player(player)
                    .build()
            );
        }
    }

    @Override
    public List<PlayerRatingResponseDTO> getRatingsByType(MatchType matchType) {
        List<PlayerRating> ratings;

        if (matchType == MatchType.SINGLES) {
            ratings = playerRatingRepository.findAllByOrderByRatingDesc();
        } else if (matchType == MatchType.DOUBLES) {
            ratings = playerRatingRepository.findAllByOrderByDoubleRatingDesc();
        } else {
            throw new IllegalArgumentException("Neznámy typ disciplíny.");
        }

        return ratings.stream()
                .map(playerRating -> {

                    Player player = playerRating.getPlayer();

                    return PlayerRatingResponseDTO.builder()
                            .playerId(player.getId())
                            .playerName(ParticipantNameUtils.buildPlayerName(player))
                            .rating(playerRating.getRating())
                            .playerLevel(PlayerLevel.fromRating(playerRating.getRating()))
                            .ratingChange(playerRating.getRatingChange())
                            .doubleRating(playerRating.getDoubleRating())
                            .doublePlayerLevel(DoublePlayerLevel.fromRating(playerRating.getDoubleRating()))
                            .doubleRatingChange(playerRating.getDoubleRatingChange())
                            .build();
                })
                .toList();
    }

    @Override
    public int calculateNewRating(int playerRating, int opponentRating, double actualScore, double marginMultiplier) {
        double expectedScore = 1.0 / (1.0 + Math.pow(10, (opponentRating - playerRating) / RATING_SCALE));

        double ratingChange = K_FACTOR * (actualScore - expectedScore) * marginMultiplier;

        return (int) Math.round(playerRating + ratingChange);
    }

    @Override
    @Transactional
    public void updateRatingsAfterMatch(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Zápas neexistuje."));

        if (match.getStatus() != MatchStatus.FINISHED) {
            throw new IllegalStateException(
                    "Rating je možné vypočítať iba pre dokončený zápas."
            );
        }

        if (match.isRatingCalculated()) {
            throw new IllegalStateException(
                    "Rating pre tento zápas už bol aktualizovaný."
            );
        }

        if (match.getResult() == null
                || match.getResult().getWinnerId() == null) {
            throw new IllegalStateException("Zápas nemá určeného víťaza.");
        }

        if (match.getMatchType() == MatchType.SINGLES) {
            updateSinglesRatingsAfterMatch(match);
        } else if (match.getMatchType() == MatchType.DOUBLES) {
            updateDoublesRatingsAfterMatch(match);
        } else {
            throw new IllegalStateException("Neznámy typ zápasu.");
        }

        match.setRatingCalculated(true);
        matchRepository.save(match);
    }

    @Override
    public void setPlayerRating(String playerId, int ratingValue) {
        setRatingValue(playerId, ratingValue, MatchType.SINGLES);
    }

    @Override
    public void setPlayerDoubleRating(String playerId, int ratingValue) {
        setRatingValue(playerId, ratingValue, MatchType.DOUBLES);
    }


    @Transactional
    @Override
    public void initializeRatingsForLeague(String leagueId, int ratingValue) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Liga neexistuje."));

        if (league.getLeagueType() == MatchType.SINGLES) {

            for (Player player : league.getPlayers()) {
                initializePlayerRating(player, ratingValue, MatchType.SINGLES);
            }

        } else if (league.getLeagueType() == MatchType.DOUBLES) {

            for (Team team : league.getTeams()) {
                initializePlayerRating(team.getPlayer1(), ratingValue, MatchType.DOUBLES);
                initializePlayerRating(team.getPlayer2(), ratingValue, MatchType.DOUBLES);
            }

        } else {
            throw new IllegalStateException("Neznámy typ ligy.");
        }
    }

    @Override
    @Transactional
    public void recalculateRatingsForLeague(String leagueId) {
        List<Match> matches = getFinishedMatchesForLeague(leagueId);

        for (Match match : matches) {
            if (match.isRatingCalculated()) {
                continue;
            }

            updateRatingsAfterMatch(match.getId());
        }
    }

    @Override
    @Transactional
    public void resetRatingCalculationForLeague(String leagueId) {
        List<Match> matches = getFinishedMatchesForLeague(leagueId);

        for (Match match : matches) {
            match.setRatingCalculated(false);

            if (match.getMatchType() == MatchType.SINGLES) {
                match.setHomePlayerRatingBefore(null);
                match.setAwayPlayerRatingBefore(null);
            } else if (match.getMatchType() == MatchType.DOUBLES) {
                match.setHomeTeamPlayer1DoubleRatingBefore(null);
                match.setHomeTeamPlayer2DoubleRatingBefore(null);
                match.setAwayTeamPlayer1DoubleRatingBefore(null);
                match.setAwayTeamPlayer2DoubleRatingBefore(null);
            }
        }

        matchRepository.saveAll(matches);
    }

    private void updatePlayerRatings(Match match, Player winner, Player loser) {
        PlayerRating winnerRating = getPlayerRating(winner.getId());

        PlayerRating loserRating = getPlayerRating(loser.getId());

        MatchResult result = match.getResult();

        int player1games = result.getSetScores().stream()
                .mapToInt(SetScore::getScore1)
                .sum();
        int player2games = result.getSetScores().stream()
                .mapToInt(SetScore::getScore2)
                .sum();

        boolean winnerIsPlayer1 = winner.getId().equals(match.getHomePlayer().getId());

        int winnerGames = winnerIsPlayer1 ? player1games : player2games;
        int loserGames = winnerIsPlayer1 ? player2games : player1games;

        double gameFactor = (double) winnerGames / (winnerGames + loserGames);

        double marginMultiplier = 1.0 + (gameFactor - 0.5);

        int oldWinnerRating = winnerRating.getRating();
        int oldLoserRating = loserRating.getRating();

        int newWinnerRating = calculateNewRating(
                winnerRating.getRating(),
                loserRating.getRating(),
                1.0,
                marginMultiplier
        );

        int newLoserRating = calculateNewRating(
                loserRating.getRating(),
                winnerRating.getRating(),
                0.0,
                marginMultiplier
        );

        // Zmena ratingu po poslednom zápase
        winnerRating.setRatingChange(newWinnerRating - oldWinnerRating);
        loserRating.setRatingChange(newLoserRating - oldLoserRating);

        // Aktualizácia aktuálneho ratingu
        winnerRating.setRating(newWinnerRating);
        loserRating.setRating(newLoserRating);

        playerRatingRepository.save(winnerRating);
        playerRatingRepository.save(loserRating);
    }

    private void updateDoublesRatings(
            Match match,
            Team winner,
            Team loser
    ) {
        PlayerRating winner1Rating = playerRatingRepository
                .findByPlayerId(winner.getPlayer1().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rating hráča neexistuje: " + winner.getPlayer1().getId()
                ));

        PlayerRating winner2Rating = playerRatingRepository
                .findByPlayerId(winner.getPlayer2().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rating hráča neexistuje: " + winner.getPlayer2().getId()
                ));

        PlayerRating loser1Rating = playerRatingRepository
                .findByPlayerId(loser.getPlayer1().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rating hráča neexistuje: " + loser.getPlayer1().getId()
                ));

        PlayerRating loser2Rating = playerRatingRepository
                .findByPlayerId(loser.getPlayer2().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rating hráča neexistuje: " + loser.getPlayer2().getId()
                ));

        MatchResult result = match.getResult();

        int player1Games = result.getSetScores().stream()
                .mapToInt(SetScore::getScore1)
                .sum();

        int player2Games = result.getSetScores().stream()
                .mapToInt(SetScore::getScore2)
                .sum();

        boolean winnerIsHome = winner.getId().equals(match.getHomeTeam().getId());

        int winnerGames = winnerIsHome ? player1Games : player2Games;
        int loserGames = winnerIsHome ? player2Games : player1Games;

        double gameFactor = (double) winnerGames / (winnerGames + loserGames);
        double marginMultiplier = 1.0 + (gameFactor - 0.5);

        // Priemerný rating každej dvojice
        double winnerAverageRating = (
                winner1Rating.getDoubleRating()
                        + winner2Rating.getDoubleRating()
        ) / 2.0;

        double loserAverageRating = (
                loser1Rating.getDoubleRating()
                        + loser2Rating.getDoubleRating()
        ) / 2.0;

        int newWinner1Rating = calculateNewRating(
                winner1Rating.getDoubleRating(),
                (int) Math.round(loserAverageRating),
                1.0,
                marginMultiplier
        );

        int newWinner2Rating = calculateNewRating(
                winner2Rating.getDoubleRating(),
                (int) Math.round(loserAverageRating),
                1.0,
                marginMultiplier
        );

        int newLoser1Rating = calculateNewRating(
                loser1Rating.getDoubleRating(),
                (int) Math.round(winnerAverageRating),
                0.0,
                marginMultiplier
        );

        int newLoser2Rating = calculateNewRating(
                loser2Rating.getDoubleRating(),
                (int) Math.round(winnerAverageRating),
                0.0,
                marginMultiplier
        );

        winner1Rating.setDoubleRatingChange(
                newWinner1Rating - winner1Rating.getDoubleRating()
        );
        winner2Rating.setDoubleRatingChange(
                newWinner2Rating - winner2Rating.getDoubleRating()
        );
        loser1Rating.setDoubleRatingChange(
                newLoser1Rating - loser1Rating.getDoubleRating()
        );
        loser2Rating.setDoubleRatingChange(
                newLoser2Rating - loser2Rating.getDoubleRating()
        );

        winner1Rating.setDoubleRating(newWinner1Rating);
        winner2Rating.setDoubleRating(newWinner2Rating);
        loser1Rating.setDoubleRating(newLoser1Rating);
        loser2Rating.setDoubleRating(newLoser2Rating);

        playerRatingRepository.save(winner1Rating);
        playerRatingRepository.save(winner2Rating);
        playerRatingRepository.save(loser1Rating);
        playerRatingRepository.save(loser2Rating);
    }

    private void updateSinglesRatingsAfterMatch(Match match) {
        Player homePlayer = match.getHomePlayer();
        Player awayPlayer = match.getAwayPlayer();

        if (homePlayer == null || awayPlayer == null) {
            throw new IllegalStateException(
                    "Singles zápas nemá oboch hráčov."
            );
        }

        Player winner;
        Player loser;

        if (homePlayer.getId().equals(match.getResult().getWinnerId())) {
            winner = homePlayer;
            loser = awayPlayer;
        } else if (awayPlayer.getId().equals(match.getResult().getWinnerId())) {
            winner = awayPlayer;
            loser = homePlayer;
        } else {
            throw new IllegalStateException(
                    "WinnerId nezodpovedá hráčovi v zápase."
            );
        }

        PlayerRating homeRating = getPlayerRating(homePlayer.getId());

        PlayerRating awayRating = getPlayerRating(awayPlayer.getId());

        if (match.getHomePlayerRatingBefore() == null) {
            match.setHomePlayerRatingBefore(homeRating.getRating());
        }

        if (match.getAwayPlayerRatingBefore() == null) {
            match.setAwayPlayerRatingBefore(awayRating.getRating());
        }

        updatePlayerRatings(match, winner, loser);
    }

    private void updateDoublesRatingsAfterMatch(Match match) {
        Team homeTeam = match.getHomeTeam();
        Team awayTeam = match.getAwayTeam();

        if (homeTeam == null || awayTeam == null) {
            throw new IllegalStateException(
                    "Doubles zápas nemá oba tímy."
            );
        }

        Team winner;
        Team loser;

        if (homeTeam.getId().equals(match.getResult().getWinnerId())) {
            winner = homeTeam;
            loser = awayTeam;
        } else if (awayTeam.getId().equals(match.getResult().getWinnerId())) {
            winner = awayTeam;
            loser = homeTeam;
        } else {
            throw new IllegalStateException(
                    "WinnerId nezodpovedá tímu v zápase."
            );
        }

        if (winner.getPlayer1() == null || winner.getPlayer2() == null
                || loser.getPlayer1() == null || loser.getPlayer2() == null) {
            throw new IllegalStateException(
                    "Oba tímy musia mať dvoch hráčov."
            );
        }

        // Načítanie ratingov domácich hráčov
        PlayerRating homePlayer1Rating = getPlayerRating(homeTeam.getPlayer1().getId());

        PlayerRating homePlayer2Rating = getPlayerRating(homeTeam.getPlayer2().getId());

        // Načítanie ratingov hosťujúcich hráčov
        PlayerRating awayPlayer1Rating = getPlayerRating(awayTeam.getPlayer1().getId());

        PlayerRating awayPlayer2Rating = getPlayerRating(awayTeam.getPlayer2().getId());

        // Uloženie pôvodných double ratingov iba raz
        if (match.getHomeTeamPlayer1DoubleRatingBefore() == null) {
            match.setHomeTeamPlayer1DoubleRatingBefore(
                    homePlayer1Rating.getDoubleRating());
        }

        if (match.getHomeTeamPlayer2DoubleRatingBefore() == null) {
            match.setHomeTeamPlayer2DoubleRatingBefore(
                    homePlayer2Rating.getDoubleRating());
        }

        if (match.getAwayTeamPlayer1DoubleRatingBefore() == null) {
            match.setAwayTeamPlayer1DoubleRatingBefore(
                    awayPlayer1Rating.getDoubleRating());
        }

        if (match.getAwayTeamPlayer2DoubleRatingBefore() == null) {
            match.setAwayTeamPlayer2DoubleRatingBefore(
                    awayPlayer2Rating.getDoubleRating());
        }

        updateDoublesRatings(match, winner, loser);
    }

    private void setRatingValue(
            String playerId,
            int ratingValue,
            MatchType matchType) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Hráč neexistuje."));

        PlayerRating playerRating = playerRatingRepository
                .findByPlayerId(playerId)
                .orElseGet(() -> PlayerRating.builder()
                        .id(UUID.randomUUID().toString())
                        .player(player)
                        .build());

        switch (matchType) {
            case SINGLES -> playerRating.setRating(ratingValue);
            case DOUBLES -> playerRating.setDoubleRating(ratingValue);
            default -> throw new IllegalArgumentException(
                    "Neznámy typ disciplíny.");
        }

        playerRatingRepository.save(playerRating);
    }

    private List<Match> getFinishedMatchesForLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Liga neexistuje."));

        MatchType matchType = league.getLeagueType();

        if (matchType != MatchType.SINGLES
                && matchType != MatchType.DOUBLES) {
            throw new IllegalStateException("Neznámy typ ligy.");
        }

        return matchRepository
                .findByLeagueIdAndStatusAndMatchTypeOrderByRoundNumberAsc(
                        leagueId,
                        MatchStatus.FINISHED,
                        matchType
                );
    }

    private void initializePlayerRating(
            Player player,
            int ratingValue,
            MatchType matchType) {

        if (player == null
                || !player.getSports().contains(Sport.TENNIS)) {
            return;
        }

        PlayerRating playerRating = playerRatingRepository
                .findByPlayerId(player.getId())
                .orElseGet(() -> PlayerRating.builder()
                        .id(UUID.randomUUID().toString())
                        .player(player)
                        .build());

        if (matchType == MatchType.SINGLES) {
            playerRating.setRating(ratingValue);
        } else if (matchType == MatchType.DOUBLES) {
            playerRating.setDoubleRating(ratingValue);
        }

        playerRatingRepository.save(playerRating);
    }

    private PlayerRating getPlayerRating(String playerId) {
        return playerRatingRepository
                .findByPlayerId(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rating hráča neexistuje: " + playerId
                ));
    }
}
