package com.example.mauri.service.impl;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.response.TeamStatsDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.service.TeamService;
import com.example.mauri.service.TeamStatsService;
import com.example.mauri.util.ParticipantNameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamStatsServiceBean implements TeamStatsService {
    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final TeamService teamService;
    private final MatchQueryService matchQueryService;
    @Override
    public TeamStatsDTO getTeamStats(
            String leagueId,
            String teamId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "League not found with id: " + leagueId));
        List<Match> matches =
                matchQueryService.getEvaluatedMatches(leagueId);
        Team team = teamService.getTeamById(teamId);
        TeamStatsDTO stats =
                calculateTeamStats(team, matches, leagueId);
        List<String> droppedIds =
                league.getDroppedParticipantsIds();
        stats.setDroppedFromLeague(
                droppedIds != null &&
                        droppedIds.contains(teamId));
        return stats;
    }
    @Override
    public List<TeamStatsDTO> getAllStatsForLeague(
            String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "League not found with id: " + leagueId));
        List<Team> teams = league.getTeams();
        List<String> droppedIds =
                league.getDroppedParticipantsIds() != null
                        ? league.getDroppedParticipantsIds()
                        : Collections.emptyList();
        List<Match> matches =
                matchQueryService.getEvaluatedMatches(leagueId);
        List<TeamStatsDTO> statsList =
                new ArrayList<>();
        for (Team team : teams) {
            TeamStatsDTO stats =
                    calculateTeamStats(
                            team,
                            matches,
                            leagueId);
            stats.setDroppedFromLeague(
                    droppedIds.contains(team.getId()));
            statsList.add(stats);
        }

        return sortLeagueTable(statsList, matches);
    }
    @Override
    public int teamProgress(
            String leagueId,
            String teamId) {
        int played =
                matchRepository.countPlayedMatchesByTeamInStatuses(
                        leagueId,
                        teamId,
                        List.of(
                                MatchStatus.FINISHED,
                                MatchStatus.CANCELLED,
                                MatchStatus.SCRATCHED
                        ));
        int total =
                matchRepository.countTotalMatchesByTeam(
                        leagueId,
                        teamId);
        if (total == 0) {
            return 0;
        }
        return (int) ((double) played / total * 100);
    }
    /**
     * =========================
     * HLAVNÉ TRIEDENIE TABUĽKY
     * =========================
     */
    private List<TeamStatsDTO> sortLeagueTable(
            List<TeamStatsDTO> statsList,
            List<Match> matches) {
/*
  Aktívne tímy
 */
        List<TeamStatsDTO> activeTeams =
                statsList.stream()
                        .filter(team ->
                                !team.isDroppedFromLeague())
                        .collect(Collectors.toList());
/*
  Odhlásené tímy
 */
        List<TeamStatsDTO> droppedTeams =
                statsList.stream()
                        .filter(
                                TeamStatsDTO::isDroppedFromLeague)
                        .toList();
/*
  HLAVNÉ KRITÉRIUM = BODY
 */
        activeTeams.sort(
                Comparator.comparingInt(
                                TeamStatsDTO::getPoints)
                        .reversed()
        );
/*
  SKUPINY ROVNAKÝCH BODOV
 */
        Map<Integer, List<TeamStatsDTO>> grouped =
                activeTeams.stream()
                        .collect(Collectors.groupingBy(
                                TeamStatsDTO::getPoints,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));
        List<TeamStatsDTO> finalOrder =
                new ArrayList<>();
/*
  Každú skupinu vyriešime samostatne
 */
        grouped.entrySet().stream()
                .sorted(Map.Entry
                        .<Integer, List<TeamStatsDTO>>
                                comparingByKey()
                        .reversed())
                .forEach(entry -> {
                    List<TeamStatsDTO> tiedTeams =
                            entry.getValue();
/*
  Bez remízy
 */
                    if (tiedTeams.size() == 1) {
                        finalOrder.addAll(
                                tiedTeams);
                    }
/*
  2 tímy -> head to head
 */
                    else if (tiedTeams.size() == 2) {
                        tiedTeams.sort((a, b) ->
                                compareHeadToHeadInMemory(
                                        a.getTeamId(),
                                        b.getTeamId(),
                                        matches));
                        finalOrder.addAll(
                                tiedTeams);
                    }
/*
  3+ tímov -> mini tabuľka
 */
                    else {
                        List<TeamStatsDTO> miniTable =
                                resolveMiniTable(
                                        tiedTeams,
                                        matches);
                        finalOrder.addAll(
                                miniTable);
                    }
                });
/*
  Odhlásené vždy na konci
 */
        finalOrder.addAll(droppedTeams);
        return finalOrder;
    }
    /**
     * =========================
     * MINI TABUĽKA
     * =========================
     */
    private List<TeamStatsDTO> resolveMiniTable(
            List<TeamStatsDTO> tiedTeams,
            List<Match> allMatches) {
        Set<String> teamIds =
                tiedTeams.stream()
                        .map(TeamStatsDTO::getTeamId)
                        .collect(Collectors.toSet());
/*
  Iba vzájomné zápasy
 */
        List<Match> mutualMatches =
                allMatches.stream()
                        .filter(match -> {
                            if (match.getHomeTeam() == null
                                    || match.getAwayTeam() == null) {
                                return false;
                            }
                            String homeId =
                                    match.getHomeTeam().getId();
                            String awayId =
                                    match.getAwayTeam().getId();
                            return teamIds.contains(homeId)
                                    && teamIds.contains(awayId);
                        })
                        .toList();
/*
  MINI BODY
 */
        Map<String, Integer> miniPoints =
                new HashMap<>();
/*
  MINI SETY
 */
        Map<String, Integer> miniSetsWon =
                new HashMap<>();
        Map<String, Integer> miniSetsLost =
                new HashMap<>();
        for (TeamStatsDTO team : tiedTeams) {
            miniPoints.put(
                    team.getTeamId(),
                    0);
            miniSetsWon.put(
                    team.getTeamId(),
                    0);
            miniSetsLost.put(
                    team.getTeamId(),
                    0);
        }
/*
  Výpočet mini tabuľky
 */
        for (Match match : mutualMatches) {
            MatchResult result =
                    match.getResult();
            if (result == null) {
                continue;
            }
            String homeId =
                    match.getHomeTeam().getId();
            String awayId =
                    match.getAwayTeam().getId();
            int homeSets =
                    result.getScore1();
            int awaySets =
                    result.getScore2();
/*
  MINI BODY
 */
            miniPoints.put(
                    homeId,
                    miniPoints.get(homeId)
                            + result.getPoints1());
            miniPoints.put(
                    awayId,
                    miniPoints.get(awayId)
                            + result.getPoints2());
/*
  MINI SETY
 */
            miniSetsWon.put(
                    homeId,
                    miniSetsWon.get(homeId)
                            + homeSets);
            miniSetsLost.put(
                    homeId,
                    miniSetsLost.get(homeId)
                            + awaySets);
            miniSetsWon.put(
                    awayId,
                    miniSetsWon.get(awayId)
                            + awaySets);
            miniSetsLost.put(
                    awayId,
                    miniSetsLost.get(awayId)
                            + homeSets);
        }
/*
  Triedenie mini tabuľky
 */
        return tiedTeams.stream()
                .sorted((a, b) -> {
                    String aId =
                            a.getTeamId();
                    String bId =
                            b.getTeamId();
/*
  1. mini body
 */
                    int cmp = Integer.compare(
                            miniPoints.get(bId),
                            miniPoints.get(aId));
                    if (cmp != 0) {
                        return cmp;
                    }
/*
  2. rozdiel setov
 */
                    int aDiff =
                            miniSetsWon.get(aId)
                                    - miniSetsLost.get(aId);
                    int bDiff =
                            miniSetsWon.get(bId)
                                    - miniSetsLost.get(bId);
                    cmp = Integer.compare(
                            bDiff,
                            aDiff);
                    if (cmp != 0) {
                        return cmp;
                    }
/*
  3. vyhrané sety
 */
                    cmp = Integer.compare(
                            miniSetsWon.get(bId),
                            miniSetsWon.get(aId));
                    if (cmp != 0) {
                        return cmp;
                    }
/*
  4. fallback
 */
                    return a.getTeamName()
                            .compareToIgnoreCase(
                                    b.getTeamName());
                })
                .toList();
    }
    /**
     * =========================
     * ŠTATISTIKY TÍMU
     * =========================
     */
    private TeamStatsDTO calculateTeamStats(
            Team team,
            List<Match> matches,
            String leagueId) {
        String teamId =
                team.getId();
        String teamName =
                ParticipantNameUtils.buildTeamShortName(
                        team);
        int matchesPlayed = 0;
        int wins = 0;
        int losses = 0;
        int setsWon = 0;
        int setsLost = 0;
        int points = 0;
        for (Match match : matches) {
            boolean isHome =
                    match.getHomeTeam() != null
                            && match.getHomeTeam()
                            .getId()
                            .equals(teamId);
            boolean isAway =
                    match.getAwayTeam() != null
                            && match.getAwayTeam()
                            .getId()
                            .equals(teamId);
            if (!isHome && !isAway) {
                continue;
            }
            MatchResult result =
                    match.getResult();
            if (result == null) {
                continue;
            }
            int teamSets =
                    isHome
                            ? result.getScore1()
                            : result.getScore2();
            int opponentSets =
                    isHome
                            ? result.getScore2()
                            : result.getScore1();
            matchesPlayed++;
            setsWon += teamSets;
            setsLost += opponentSets;
            points += isHome
                    ? result.getPoints1()
                    : result.getPoints2();
            if (teamId.equals(
                    result.getWinnerId())) {
                wins++;
            } else {
                losses++;
            }
        }
        int progress =
                teamProgress(
                        leagueId,
                        teamId);
        return TeamStatsDTO.builder()
                .teamId(teamId)
                .teamName(teamName)
                .matches(matchesPlayed)
                .wins(wins)
                .losses(losses)
                .points(points)
                .setsWon(setsWon)
                .setsLost(setsLost)
                .leagueProgress(progress)
                .build();
    }
    /**
     * =========================
     * HEAD TO HEAD
     * =========================
     */
    private int compareHeadToHeadInMemory(
            String teamId1,
            String teamId2,
            List<Match> allMatches) {
        int pointsTeam1 = 0;
        int pointsTeam2 = 0;
        for (Match match : allMatches) {
            if (match.getHomeTeam() == null
                    || match.getAwayTeam() == null) {
                continue;
            }
            boolean isBetweenTeams =
                    (match.getHomeTeam().getId()
                            .equals(teamId1)
                            &&
                            match.getAwayTeam().getId()
                                    .equals(teamId2))
                            ||
                            (match.getHomeTeam().getId()
                                    .equals(teamId2)
                                    &&
                                    match.getAwayTeam().getId()
                                            .equals(teamId1));
            if (!isBetweenTeams) {
                continue;
            }
            MatchResult result =
                    match.getResult();
            if (result == null) {
                continue;
            }
            boolean team1Home =
                    match.getHomeTeam().getId()
                            .equals(teamId1);
            pointsTeam1 += team1Home
                    ? result.getPoints1()
                    : result.getPoints2();
            pointsTeam2 += team1Home
                    ? result.getPoints2()
                    : result.getPoints1();
        }
        return Integer.compare(
                pointsTeam2,
                pointsTeam1);
    }
}


//@Service
//@RequiredArgsConstructor
//public class TeamStatsServiceBean implements TeamStatsService {
//
//    private final MatchRepository matchRepository;
//    private final LeagueRepository leagueRepository;
//    private final TeamService teamService;
//    private final MatchQueryService matchQueryService;
//
//    // Získanie štatistík jedného tímu v lige
//    @Override
//    public TeamStatsDTO getTeamStats(String leagueId, String teamId) {
//        League league = leagueRepository.findById(leagueId)
//                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));
//
//        List<Match> matches = matchQueryService.getEvaluatedMatches(leagueId);
//        Team team = teamService.getTeamById(teamId);
//
//        TeamStatsDTO stats = calculateTeamStats(team, matches, leagueId);
//
//        List<String> droppedIds = league.getDroppedParticipantsIds();
//        stats.setDroppedFromLeague(droppedIds != null && droppedIds.contains(teamId));
//
//        return stats;
//    }
//
//    // Získanie štatistík všetkých tímov v lige
//    @Override
//    public List<TeamStatsDTO> getAllStatsForLeague(String leagueId) {
//        League league = leagueRepository.findById(leagueId)
//                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));
//
//        List<Team> teams = league.getTeams();
//        List<String> droppedIds = league.getDroppedParticipantsIds() != null
//                ? league.getDroppedParticipantsIds()
//                : Collections.emptyList();
//
//        List<Match> matches = matchQueryService.getEvaluatedMatches(leagueId);
//
//        List<TeamStatsDTO> statsList = new ArrayList<>();
//
//        for (Team team : teams) {
//            TeamStatsDTO stats = calculateTeamStats(team, matches, leagueId);
//
//            // ✅ Nastavíme príznak odhlásenia
//            stats.setDroppedFromLeague(droppedIds.contains(team.getId()));
//
//            statsList.add(stats);
//        }
//
//        // ✅ Triedenie
//        statsList.sort((a, b) -> {
//            boolean aDropped = a.isDroppedFromLeague();
//            boolean bDropped = b.isDroppedFromLeague();
//
//            if (aDropped && !bDropped) return 1;
//            if (!aDropped && bDropped) return -1;
//
//            // Podľa počtu vyhraných setov
//            int cmp = Integer.compare(b.getSetsWon(), a.getSetsWon());
//            if (cmp != 0) return cmp;
//
//            // Podľa vzájomného zápasu
//            return compareHeadToHeadInMemory(a.getTeamId(), b.getTeamId(), matches);
//        });
//
//        return statsList;
//    }
//
//    @Override
//    public int teamProgress(String leagueId, String teamId) {
//        int played = matchRepository.countPlayedMatchesByTeamInStatuses(
//                leagueId,
//                teamId,
//                List.of(MatchStatus.FINISHED,MatchStatus.CANCELLED,MatchStatus.SCRATCHED));
//        int total = matchRepository.countTotalMatchesByTeam(leagueId,teamId);
//
//        if (total == 0) return 0;
//
//        return (int) ((double) played / total * 100);
//    }
//
//    // Privátna metóda na výpočet štatistík tímu zo zoznamu zápasov
//    private TeamStatsDTO calculateTeamStats(Team team, List<Match> matches, String leagueId) {
//        String teamId = team.getId();
//        String teamName = ParticipantNameUtils.buildTeamShortName(team);
//
//        int matchesPlayed = 0;
//        int wins = 0;
//        int losses = 0;
//        int setsWon = 0;
//        int setsLost = 0;
//
//        for (Match match : matches) {
//            boolean isHome = match.getHomeTeam() != null && match.getHomeTeam().getId().equals(teamId);
//            boolean isAway = match.getAwayTeam() != null && match.getAwayTeam().getId().equals(teamId);
//            if (!isHome && !isAway) continue;
//
//            MatchResult result = match.getResult();
//            if (result == null) continue;
//
//            int teamSets = isHome ? result.getScore1() : result.getScore2();
//            int opponentSets = isHome ? result.getScore2() : result.getScore1();
//
//            matchesPlayed++;
//            setsWon += teamSets;
//            setsLost += opponentSets;
//
//            if (teamId.equals(result.getWinnerId())) wins++;
//            else losses++;
//        }
//
//        int progress = teamProgress(leagueId,teamId);
//
//        return TeamStatsDTO.builder()
//                .teamId(teamId)
//                .teamName(teamName)
//                .matches(matchesPlayed)
//                .wins(wins)
//                .losses(losses)
//                .setsWon(setsWon)
//                .setsLost(setsLost)
//                .leagueProgress(progress)
//                .build();
//    }
//
//    private int compareHeadToHeadInMemory(String teamId1, String teamId2, List<Match> allMatches) {
//        int winsTeam1 = 0;
//        int winsTeam2 = 0;
//
//        for (Match match : allMatches) {
//            if (match.getHomeTeam() == null || match.getAwayTeam() == null) continue;
//
//            boolean isBetweenTeams =
//                    (match.getHomeTeam().getId().equals(teamId1) && match.getAwayTeam().getId().equals(teamId2)) ||
//                            (match.getHomeTeam().getId().equals(teamId2) && match.getAwayTeam().getId().equals(teamId1));
//
//            if (!isBetweenTeams) continue;
//
//            MatchResult result = match.getResult();
//            if (result == null) continue;
//
//            String winnerId = result.getWinnerId();
//            if (winnerId.equals(teamId1)) winsTeam1++;
//            else if (winnerId.equals(teamId2)) winsTeam2++;
//        }
//
//        if (winsTeam1 > winsTeam2) return -1;  // teamId1 lepší
//        else if (winsTeam2 > winsTeam1) return 1; // teamId2 lepší
//        else return 0; // nerozhodné
//    }
//}

