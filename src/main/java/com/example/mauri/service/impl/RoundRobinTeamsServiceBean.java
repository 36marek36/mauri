package com.example.mauri.service.impl;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.model.Match;
import com.example.mauri.model.Team;
import com.example.mauri.service.RoundRobinTeamsService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoundRobinTeamsServiceBean implements RoundRobinTeamsService {

    public List<Match> generateMatches(List<Team> teams, String leagueId, MatchType matchType) {

        int numTeams = teams.size();

        if (numTeams % 2 != 0) {
            teams = new ArrayList<>(teams); // clone
            teams.add(null); // voľno
            numTeams++;
        }

        int rounds = numTeams - 1;
        int halfSize = numTeams / 2;
        Team fixed = teams.get(0);
        List<Team> rotated = new ArrayList<>(teams);
        rotated.remove(0);

        List<RoundRobinTeamsServiceBean.Pair> pairs = new ArrayList<>();

        for (int round = 0; round < rounds; round++) {
            List<Team> roundTeams = new ArrayList<>();
            roundTeams.add(fixed);
            roundTeams.addAll(rotated);

            for (int i = 0; i < halfSize; i++) {
                Team t1 = roundTeams.get(i);
                Team t2 = roundTeams.get(numTeams - 1 - i);
                if (t1 == null || t2 == null) continue;
                pairs.add(new RoundRobinTeamsServiceBean.Pair(t1, t2, round + 1));
            }

            rotated.add(0, rotated.remove(rotated.size() - 1));
        }

        int totalMatchesPerTeam = numTeams - 1;
        int maxHome = (totalMatchesPerTeam + 1) / 2;

        Map<String, Integer> homeCount = new HashMap<>();
        Map<String, Integer> awayCount = new HashMap<>();
        for (Team p : teams) {
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
        for (Team t : teams) {
            if (t == null) continue;
            long home = matches.stream().filter(m -> m.getHomeTeam().equals(t)).count();
            long away = matches.stream().filter(m -> m.getAwayTeam().equals(t)).count();
            System.out.println(t + ": Home = " + home + ", Away = " + away);
        }

        return matches;
    }

    private boolean assignHomeAway(int index, List<RoundRobinTeamsServiceBean.Pair> pairs,
                                   Map<String, Integer> homeCount,
                                   Map<String, Integer> awayCount,
                                   List<Match> result,
                                   String leagueId,
                                   MatchType matchType,
                                   int maxHome) {
        if (index == pairs.size()) return true;

        RoundRobinTeamsServiceBean.Pair pair = pairs.get(index);
        Team t1 = pair.t1;
        Team t2 = pair.t2;

        String id1 = t1.getId();
        String id2 = t2.getId();

        int h1 = homeCount.get(id1);
        int a1 = awayCount.get(id1);
        int h2 = homeCount.get(id2);
        int a2 = awayCount.get(id2);

        // Skús t1 ako domáceho
        if (h1 < maxHome && a2 < maxHome && isBalanced(h1 + 1, a1) && isBalanced(h2, a2 + 1)) {
            Match m = createMatch(t1, t2, pair.round, leagueId, matchType);
            result.add(m);
            homeCount.put(id1, h1 + 1);
            awayCount.put(id2, a2 + 1);

            if (assignHomeAway(index + 1, pairs, homeCount, awayCount, result, leagueId, matchType, maxHome))
                return true;

            result.remove(result.size() - 1);
            homeCount.put(id1, h1);
            awayCount.put(id2, a2);
        }

        // Skús t2 ako domáceho
        if (h2 < maxHome && a1 < maxHome && isBalanced(h2 + 1, a2) && isBalanced(h1, a1 + 1)) {
            Match m = createMatch(t2, t1, pair.round, leagueId, matchType);
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

    private Match createMatch(Team home, Team away, int round, String leagueId, MatchType matchType) {
        Match match = new Match();
        match.setId(UUID.randomUUID().toString());
        match.setLeagueId(leagueId);
        match.setMatchType(matchType);
        match.setStatus(MatchStatus.CREATED);
        match.setRoundNumber(round);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        return match;
    }

    private static class Pair {
        Team t1, t2;
        int round;

        Pair(Team t1, Team t2, int round) {
            this.t1 = t1;
            this.t2 = t2;
            this.round = round;
        }
    }
}

