package com.example.mauri.service.impl;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.enums.Sport;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.*;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.repository.PlayerRatingRepository;
import com.example.mauri.service.PlayerRatingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerRatingServiceBean implements PlayerRatingService {
    private final PlayerRatingRepository playerRatingRepository;
    private final LeagueRepository leagueRepository;
    private final MatchRepository matchRepository;
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
    public int calculateNewRating(int playerRating, int opponentRating, double actualScore) {
        double expectedScore = 1.0 / (1.0 + Math.pow(10, (opponentRating - playerRating) / RATING_SCALE));

        return (int) Math.round(playerRating + K_FACTOR * (actualScore - expectedScore));
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

        // Rating zatiaľ počítame iba pre singles.
        if (match.getMatchType() != MatchType.SINGLES) {
            return;
        }

        if (match.getResult() == null || match.getResult().getWinnerId() == null) {
            throw new IllegalStateException(
                    "Zápas nemá určeného víťaza."
            );
        }

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

        updatePlayerRatings(winner, loser);

        match.setRatingCalculated(true);
    }

    @Override
    @Transactional
    public void updatePlayerRatings(Player winner, Player loser) {
        PlayerRating winnerRating = playerRatingRepository
                .findByPlayerId(winner.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rating víťaza neexistuje."
                        ));

        PlayerRating loserRating = playerRatingRepository
                .findByPlayerId(loser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rating porazeného neexistuje."
                        ));

        int newWinnerRating = calculateNewRating(
                winnerRating.getRating(),
                loserRating.getRating(),
                1.0
        );

        int newLoserRating = calculateNewRating(
                loserRating.getRating(),
                winnerRating.getRating(),
                0.0
        );

        winnerRating.setRating(newWinnerRating);
        loserRating.setRating(newLoserRating);

        playerRatingRepository.save(winnerRating);
        playerRatingRepository.save(loserRating);
    }

    @Transactional
    @Override
    public void initializeRatingsForLeague(String leagueId, int ratingValue) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Liga neexistuje."));

        for (Player player : league.getPlayers()) {

            if (!player.getSports().contains(Sport.TENNIS)) {
                continue;
            }

            PlayerRating playerRating = playerRatingRepository
                    .findByPlayerId(player.getId())
                    .orElseGet(() -> PlayerRating.builder()
                            .id(UUID.randomUUID().toString())
                            .player(player)
                            .build());

            playerRating.setRating(ratingValue);

            playerRatingRepository.save(playerRating);
        }
    }

    @Override
    @Transactional
    public void recalculateRatingsForLeague(String leagueId) {
        List<Match> matches = matchRepository
                .findByLeagueIdAndStatusAndMatchTypeOrderByRoundNumberAsc(
                        leagueId,
                        MatchStatus.FINISHED,
                        MatchType.SINGLES
                );

        for (Match match : matches) {

            if (match.isRatingCalculated()) {
                continue;
            }

            MatchResult result = match.getResult();

            if (result == null || result.getWinnerId() == null) {
                continue;
            }

            Player homePlayer = match.getHomePlayer();
            Player awayPlayer = match.getAwayPlayer();

            if (homePlayer == null || awayPlayer == null) {
                continue;
            }

            Player winner;
            Player loser;

            if (homePlayer.getId().equals(result.getWinnerId())) {
                winner = homePlayer;
                loser = awayPlayer;
            } else if (awayPlayer.getId().equals(result.getWinnerId())) {
                winner = awayPlayer;
                loser = homePlayer;
            } else {
                throw new IllegalStateException(
                        "WinnerId nezodpovedá hráčom v zápase " + match.getId()
                );
            }

            updatePlayerRatings(winner, loser);

            match.setRatingCalculated(true);
        }
    }

    @Override
    @Transactional
    public void resetRatingCalculationForLeague(String leagueId) {
        List<Match> matches = matchRepository
                .findByLeagueIdAndStatusAndMatchTypeOrderByRoundNumberAsc(
                        leagueId,
                        MatchStatus.FINISHED,
                        MatchType.SINGLES
                );

        for (Match match : matches) {
            match.setRatingCalculated(false);
        }
    }
}
