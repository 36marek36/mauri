package com.example.mauri.service.impl;

import com.example.mauri.enums.MatchType;
import com.example.mauri.model.Match;
import com.example.mauri.model.Player;
import com.example.mauri.service.RoundRobinPlayersService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoundRobinPlayersServiceBean implements RoundRobinPlayersService {

    public List<Match> generateMatches(List<Player> players, String leagueId, MatchType matchType) {

        int numPlayers = players.size();

        if (numPlayers % 2 != 0) {
            players = new ArrayList<>(players); // clone
            players.add(null); // voľno
            numPlayers++;
        }

        int rounds = numPlayers - 1;
        int halfSize = numPlayers / 2;
        Player fixed = players.get(0);
        List<Player> rotated = new ArrayList<>(players);
        rotated.remove(0);

        List<Pair> pairs = new ArrayList<>();

        for (int round = 0; round < rounds; round++) {
            List<Player> roundPlayers = new ArrayList<>();
            roundPlayers.add(fixed);
            roundPlayers.addAll(rotated);

            for (int i = 0; i < halfSize; i++) {
                Player p1 = roundPlayers.get(i);
                Player p2 = roundPlayers.get(numPlayers - 1 - i);
                if (p1 == null || p2 == null) continue;
                pairs.add(new Pair(p1, p2, round + 1));
            }

            rotated.add(0, rotated.remove(rotated.size() - 1));
        }

        int totalMatchesPerPlayer = numPlayers - 1;
        int maxHome = (totalMatchesPerPlayer + 1) / 2;

        Map<String, Integer> homeCount = new HashMap<>();
        Map<String, Integer> awayCount = new HashMap<>();
        for (Player p : players) {
            if (p != null) {
                homeCount.put(p.getId(), 0);
                awayCount.put(p.getId(), 0);
            }
        }

        List<Match> matches = new ArrayList<>();
        if (!assignHomeAway(0, pairs, homeCount, awayCount, matches, leagueId, matchType, maxHome)) {
            throw new RuntimeException("Nepodarilo sa vygenerovať rovnomerný rozpis.");
        }

        // Výpis do konzoly (debug)
        for (Player p : players) {
            if (p == null) continue;
            long home = matches.stream().filter(m -> m.getHomePlayer().equals(p)).count();
            long away = matches.stream().filter(m -> m.getAwayPlayer().equals(p)).count();
            System.out.println(p + ": Home = " + home + ", Away = " + away);
        }

        return matches;
    }

    private boolean assignHomeAway(int index, List<Pair> pairs,
                                   Map<String, Integer> homeCount,
                                   Map<String, Integer> awayCount,
                                   List<Match> result,
                                   String leagueId,
                                   MatchType matchType,
                                   int maxHome) {
        if (index == pairs.size()) return true;

        Pair pair = pairs.get(index);
        Player p1 = pair.p1;
        Player p2 = pair.p2;

        String id1 = p1.getId();
        String id2 = p2.getId();

        int h1 = homeCount.get(id1);
        int a1 = awayCount.get(id1);
        int h2 = homeCount.get(id2);
        int a2 = awayCount.get(id2);

        // Skús p1 ako domáceho
        if (h1 < maxHome && a2 < maxHome && isBalanced(h1 + 1, a1) && isBalanced(h2, a2 + 1)) {
            Match m = createMatch(p1, p2, pair.round, leagueId, matchType);
            result.add(m);
            homeCount.put(id1, h1 + 1);
            awayCount.put(id2, a2 + 1);

            if (assignHomeAway(index + 1, pairs, homeCount, awayCount, result, leagueId, matchType, maxHome))
                return true;

            result.remove(result.size() - 1);
            homeCount.put(id1, h1);
            awayCount.put(id2, a2);
        }

        // Skús p2 ako domáceho
        if (h2 < maxHome && a1 < maxHome && isBalanced(h2 + 1, a2) && isBalanced(h1, a1 + 1)) {
            Match m = createMatch(p2, p1, pair.round, leagueId, matchType);
            result.add(m);
            homeCount.put(id2, h2 + 1);
            awayCount.put(id1, a1 + 1);

            if (assignHomeAway(index + 1, pairs, homeCount, awayCount, result, leagueId, matchType, maxHome))
                return true;

            result.remove(result.size() - 1);
            homeCount.put(id2, h2);
            awayCount.put(id1, a1);
        }

        return false;
    }

    private boolean isBalanced(int home, int away) {
        return Math.abs(home - away) <= 1;
    }

    private Match createMatch(Player home, Player away, int round, String leagueId, MatchType matchType) {
        //        Match match = new Match();
//        match.setId(UUID.randomUUID().toString());
//        match.setLeagueId(leagueId);
//        match.setMatchType(matchType);
//        match.setStatus(MatchStatus.CREATED);
//        match.setRoundNumber(round);
//        match.setHomePlayer(home);
//        match.setAwayPlayer(away);
        return Match.builder()
                .id(UUID.randomUUID().toString())
                .leagueId(leagueId)
                .matchType(matchType)
                .roundNumber(round)
                .homePlayer(home)
                .awayPlayer(away)
                .build();
    }

    private static class Pair {
        Player p1, p2;
        int round;

        Pair(Player p1, Player p2, int round) {
            this.p1 = p1;
            this.p2 = p2;
            this.round = round;
        }
    }

}
