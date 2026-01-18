package com.example.mauri.service.impl;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.response.PlayerStatsDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.service.PlayerStatsService;
import com.example.mauri.util.ParticipantNameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerStatsServiceBean implements PlayerStatsService {

    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final MatchQueryService matchQueryService;

    // Získanie štatistík jedného hráča v lige
    @Override
    public PlayerStatsDTO getPlayerStats(String leagueId, String playerId) {
        // Získaj všetky štatistiky pre ligu (vrátane ranku)
        List<PlayerStatsDTO> leagueStats = getAllStatsForLeague(leagueId);

        // Nájdeme konkrétneho hráča
        return leagueStats.stream()
                .filter(s -> s.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Player with id " + playerId + " not found in league " + leagueId));
    }

    // Získanie štatistík všetkých hráčov v lige
    @Override
    public List<PlayerStatsDTO> getAllStatsForLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));

        List<Player> players = league.getPlayers();
        List<String> droppedIds = league.getDroppedParticipantsIds() != null
                ? league.getDroppedParticipantsIds()
                : Collections.emptyList();

        // Načíta všetky vyhodnotené zápasy ligy
        List<Match> matches = matchQueryService.getEvaluatedMatches(leagueId);

        List<PlayerStatsDTO> statsList = new ArrayList<>();

        for (Player player : players) {
            PlayerStatsDTO stats = calculatePlayerStats(player, matches, leagueId);

            // ✅ nastavíme, či je hráč odhlásený
            stats.setDroppedFromLeague(droppedIds.contains(player.getId()));

            statsList.add(stats);
        }

        // ✅ Triedenie - najprv aktívni, potom odhlásení
        statsList.sort((a, b) -> {
            boolean aDropped = a.isDroppedFromLeague();
            boolean bDropped = b.isDroppedFromLeague();

            if (aDropped && !bDropped) return 1;
            if (!aDropped && bDropped) return -1;

            // Inak triedenie podľa vyhraných setov
            int cmp = Integer.compare(b.getSetsWon(), a.getSetsWon());
            if (cmp != 0) return cmp;

            // Ak sú sety rovnaké, porovnaj vzájomný zápas
            return compareHeadToHeadInMemory(a.getPlayerId(), b.getPlayerId(), matches);
        });
        // ✅ Po triedení priraď poradie (rank)
        int rank = 1;
        for (PlayerStatsDTO stats : statsList) {
            if (!stats.isDroppedFromLeague()) {
                stats.setRank(rank++);
            } else {
                // Ak chceš, aby odhlásení nemali poradie, nechaj null
                stats.setRank(null);
            }
        }

        return statsList;
    }

    @Override
    public int playerProgress(String leagueId, String playerId) {
        int played = matchRepository.countPlayedMatchesByPlayerInStatuses(
                leagueId,
                playerId,
                List.of(MatchStatus.FINISHED, MatchStatus.CANCELLED, MatchStatus.SCRATCHED));
        int total = matchRepository.countTotalMatchesByPlayer(leagueId, playerId);

        if (total == 0) return 0;

        return (int) ((double) played / total * 100);
    }

    // Privátna metóda na výpočet štatistík hráča zo zoznamu zápasov
    private PlayerStatsDTO calculatePlayerStats(Player player, List<Match> matches, String leagueId) {
        String playerId = player.getId();
        String playerName = ParticipantNameUtils.buildPlayerName(player);

        int matchesPlayed = 0;
        int wins = 0;
        int losses = 0;
        int setsWon = 0;
        int setsLost = 0;

        for (Match match : matches) {
            boolean isHome = match.getHomePlayer() != null && match.getHomePlayer().getId().equals(playerId);
            boolean isAway = match.getAwayPlayer() != null && match.getAwayPlayer().getId().equals(playerId);
            if (!isHome && !isAway) continue;

            MatchResult result = match.getResult();
            if (result == null) continue;

            int playerSets = isHome ? result.getScore1() : result.getScore2();
            int opponentSets = isHome ? result.getScore2() : result.getScore1();

            matchesPlayed++;
            setsWon += playerSets;
            setsLost += opponentSets;

            if (playerId.equals(result.getWinnerId())) wins++;
            else losses++;
        }

        int progress = playerProgress(leagueId, playerId);

        return PlayerStatsDTO.builder()
                .playerId(playerId)
                .playerName(playerName)
                .matches(matchesPlayed)
                .wins(wins)
                .losses(losses)
                .setsWon(setsWon)
                .setsLost(setsLost)
                .leagueProgress(progress)
                .build();
    }

    /**
     * Porovnáva dvoch hráčov podľa vzájomných zápasov.
     * Ak má hráč1 viac víťazstiev v zápasoch proti hráčovi2, hráč1 je lepší.
     */
    private int compareHeadToHeadInMemory(String playerId1, String playerId2, List<Match> allMatches) {
        int winsPlayer1 = 0;
        int winsPlayer2 = 0;

        for (Match match : allMatches) {
            if (match.getHomePlayer() == null || match.getAwayPlayer() == null) continue;

            boolean isBetweenPlayers =
                    (match.getHomePlayer().getId().equals(playerId1) && match.getAwayPlayer().getId().equals(playerId2)) ||
                            (match.getHomePlayer().getId().equals(playerId2) && match.getAwayPlayer().getId().equals(playerId1));

            if (!isBetweenPlayers) continue;

            MatchResult result = match.getResult();
            if (result == null) continue;

            String winnerId = result.getWinnerId();
            if (winnerId.equals(playerId1)) winsPlayer1++;
            else if (winnerId.equals(playerId2)) winsPlayer2++;
        }

        if (winsPlayer1 > winsPlayer2) return -1;  // player1 lepší
        else if (winsPlayer2 > winsPlayer1) return 1; // player2 lepší
        else return 0;  // nerozhodné
    }
}

