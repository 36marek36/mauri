package com.example.mauri.service.volley.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.VolleyLeague;
import com.example.mauri.model.VolleyMatch;
import com.example.mauri.model.VolleyMatchResult;
import com.example.mauri.model.VolleyTeam;
import com.example.mauri.model.dto.response.VolleyTeamStatsDTO;
import com.example.mauri.repository.VolleyLeagueRepository;
import com.example.mauri.repository.VolleyMatchRepository;
import com.example.mauri.service.volley.VolleyLeagueStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolleyLeagueStatsServiceBean implements VolleyLeagueStatsService {
    private final VolleyLeagueRepository volleyLeagueRepository;
    private final VolleyMatchRepository volleyMatchRepository;

    @Override
    public VolleyTeamStatsDTO getTeamStats(String leagueId, String teamId) {
        List<VolleyTeamStatsDTO> leagueStats = getAllStatsForLeague(leagueId);
        return leagueStats.stream()
                .filter(s -> s.getTeamId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tím s id " + teamId + " sa nenašiel v lige " + leagueId));
    }

    /**
     * =========================
     * TABUĽKA LIGY (VOLEJBAL)
     * =========================
     */
    @Override
    public List<VolleyTeamStatsDTO> getAllStatsForLeague(String leagueId) {
        VolleyLeague league = volleyLeagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("Liga nebola nájdená: " + leagueId));

        Set<VolleyTeam> teams = league.getTeams();
        List<String> droppedIds = league.getDroppedTeamIds() != null
                ? league.getDroppedTeamIds().stream().map(String::valueOf).collect(Collectors.toList())
                : Collections.emptyList();

        List<VolleyMatch> matches = volleyMatchRepository.findByVolleyLeagueId(leagueId);
        List<VolleyMatch> evaluatedMatches = matches.stream()
                .filter(m -> m.getStatus().isPlayed())
                .toList();

        List<VolleyTeamStatsDTO> statsList = new ArrayList<>();

        for (VolleyTeam team : teams) {
            VolleyTeamStatsDTO stats = calculateVolleyballStats(team, evaluatedMatches);
            stats.setDroppedFromLeague(droppedIds.contains(String.valueOf(team.getId())));
            statsList.add(stats);
        }

        // Pre zjednodušenie triedenia odovzdáme aj zoznam droppedIds
        List<VolleyTeamStatsDTO> sorted = sortLeagueTable(statsList, evaluatedMatches);

        // V DTO nemáte pole 'rank'. Ak ho budete potrebovať, musíte ho do DTO pridať,
        // alebo poradie určiť čisto indexom v tomto vrátenom zozname.
        return sorted;
    }

    /**
     * =========================
     * VÝPOČET ŠTATISTÍK TÍMU
     * =========================
     */
    private VolleyTeamStatsDTO calculateVolleyballStats(VolleyTeam team, List<VolleyMatch> matches) {
        int matchesCount = 0;
        int wins = 0;
        int losses = 0;
        int points = 0;
        int setsWon = 0;
        int setsLost = 0;

        for (VolleyMatch match : matches) {
            if (match.getHomeTeam() == null || match.getAwayTeam() == null || match.getResult() == null) {
                continue;
            }

            VolleyMatchResult res = match.getResult();
            boolean isHome = match.getHomeTeam().getId().equals(team.getId());
            boolean isAway = match.getAwayTeam().getId().equals(team.getId());

            if (!isHome && !isAway) continue;

            matchesCount++;

            // Využitie vašich predpočítaných setov
            int homeSets = res.getHomeTeamScore() != null ? res.getHomeTeamScore() : 0;
            int awaySets = res.getAwayTeamScore() != null ? res.getAwayTeamScore() : 0;

            if (isHome) {
                setsWon += homeSets;
                setsLost += awaySets;
                // Využitie vašich predpočítaných bodov (netreba znova prepočítavať)
                points += res.getHomeTeamPoints() != null ? res.getHomeTeamPoints() : 0;

                if (homeSets > awaySets) wins++;
                else losses++;
            } else {
                setsWon += awaySets;
                setsLost += homeSets;
                // Využitie vašich predpočítaných bodov
                points += res.getAwayTeamPoints() != null ? res.getAwayTeamPoints() : 0;

                if (awaySets > homeSets) wins++;
                else losses++;
            }
        }

        return VolleyTeamStatsDTO.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .matches(matchesCount)
                .wins(wins)
                .losses(losses)
                .points(points)
                .setsWon(setsWon)
                .setsLost(setsLost)
                .build();
    }

    /**
     * =========================
     * HLAVNÉ TRIEDENIE
     * =========================
     */
    private List<VolleyTeamStatsDTO> sortLeagueTable(
            List<VolleyTeamStatsDTO> statsList,
            List<VolleyMatch> matches) {

        // Rozdelenie na aktívnych a odhlásených priamo pomocou nového príznaku v DTO
        List<VolleyTeamStatsDTO> activeTeams = statsList.stream()
                .filter(p -> !p.isDroppedFromLeague()) // 🟢 Použitie nového príznaku
                .collect(Collectors.toList());

        List<VolleyTeamStatsDTO> droppedTeams = statsList.stream()
                .filter(VolleyTeamStatsDTO::isDroppedFromLeague) // 🟢 Použitie nového príznaku
                .toList();

        // Zoskupenie aktívnych podľa bodov
        Map<Integer, List<VolleyTeamStatsDTO>> grouped = activeTeams.stream()
                .collect(Collectors.groupingBy(
                        VolleyTeamStatsDTO::getPoints,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<VolleyTeamStatsDTO> finalOrder = new ArrayList<>();

        grouped.entrySet().stream()
                .sorted(Map.Entry.<Integer, List<VolleyTeamStatsDTO>>comparingByKey().reversed())
                .forEach(entry -> {
                    List<VolleyTeamStatsDTO> tiedTeams = entry.getValue();

                    if (tiedTeams.size() == 1) {
                        finalOrder.addAll(tiedTeams);
                    } else if (tiedTeams.size() == 2) {
                        tiedTeams.sort((a, b) -> compareHeadToHeadInMemory(
                                a.getTeamId(),
                                b.getTeamId(),
                                matches));
                        finalOrder.addAll(tiedTeams);
                    } else {
                        List<VolleyTeamStatsDTO> miniTable = resolveMiniTable(tiedTeams, matches);
                        finalOrder.addAll(miniTable);
                    }
                });

        // Odhlásené tímy padnú na koniec tabuľky
        finalOrder.addAll(droppedTeams);
        return finalOrder;
    }

    /**
     * =========================
     * MINI TABUĽKA VZÁJOMNÝCH ZÁPASOV
     * =========================
     */
    private List<VolleyTeamStatsDTO> resolveMiniTable(List<VolleyTeamStatsDTO> tiedTeams, List<VolleyMatch> allMatches) {
        Set<String> teamIds = tiedTeams.stream()
                .map(VolleyTeamStatsDTO::getTeamId)
                .collect(Collectors.toSet());

        // Filtrujeme iba vzájomné zápasy medzi tímami v mini-tabuľke
        List<VolleyMatch> mutualMatches = allMatches.stream()
                .filter(match -> match.getHomeTeam() != null && match.getAwayTeam() != null
                        && teamIds.contains(match.getHomeTeam().getId())
                        && teamIds.contains(match.getAwayTeam().getId()))
                .toList();

        Map<String, Integer> miniPoints = new HashMap<>();
        Map<String, Integer> miniSetsWon = new HashMap<>();
        Map<String, Integer> miniSetsLost = new HashMap<>();

        for (VolleyTeamStatsDTO team : tiedTeams) {
            String tid = team.getTeamId();
            miniPoints.put(tid, 0);
            miniSetsWon.put(tid, 0);
            miniSetsLost.put(tid, 0);
        }

        for (VolleyMatch match : mutualMatches) {
            if (match.getResult() == null) continue;
            VolleyMatchResult res = match.getResult();

            String homeId = match.getHomeTeam().getId();
            String awayId = match.getAwayTeam().getId();

            int homeSets = res.getHomeTeamScore() != null ? res.getHomeTeamScore() : 0;
            int awaySets = res.getAwayTeamScore() != null ? res.getAwayTeamScore() : 0;
            int homePoints = res.getHomeTeamPoints() != null ? res.getHomeTeamPoints() : 0;
            int awayPoints = res.getAwayTeamPoints() != null ? res.getAwayTeamPoints() : 0;

            miniSetsWon.merge(homeId, homeSets, Integer::sum);
            miniSetsLost.merge(homeId, awaySets, Integer::sum);
            miniSetsWon.merge(awayId, awaySets, Integer::sum);
            miniSetsLost.merge(awayId, homeSets, Integer::sum);

            miniPoints.merge(homeId, homePoints, Integer::sum);
            miniPoints.merge(awayId, awayPoints, Integer::sum);
        }

        List<VolleyTeamStatsDTO> sortedMini = new ArrayList<>(tiedTeams);
        sortedMini.sort((a, b) -> {
            String idA = a.getTeamId();
            String idB = b.getTeamId();

            // 1. Kritérium: Body v mini-tabuľke
            int pointsCompare = Integer.compare(miniPoints.get(idB), miniPoints.get(idA));
            if (pointsCompare != 0) return pointsCompare;

            // 2. Kritérium: POMER SETOV (Krížové násobenie bráni deleniu nulou a double nepresnostiam)
            // Matematicky: (WonA / LostA) vs (WonB / LostB) => WonA * LostB vs WonB * LostA
            int wonA = miniSetsWon.get(idA);
            int lostA = miniSetsLost.get(idA);
            int wonB = miniSetsWon.get(idB);
            int lostB = miniSetsLost.get(idB);

            long ratioA = (long) wonA * lostB;
            long ratioB = (long) wonB * lostA;

            int ratioCompare = Long.compare(ratioB, ratioA); // Obrátené poradie pre zostupné triedenie
            if (ratioCompare != 0) return ratioCompare;

            // 3. Kritérium: Vyšší počet vyhratých setov v mini-tabuľke (ak by bol pomer zhodný)
            return Integer.compare(miniSetsWon.get(idB), miniSetsWon.get(idA));
        });

        return sortedMini;
    }

    /**
     * Vzájomný zápas dvoch tímov
     */
    private int compareHeadToHeadInMemory(String teamA, String teamB, List<VolleyMatch> matches) {
        for (VolleyMatch m : matches) {
            if (m.getHomeTeam() == null || m.getAwayTeam() == null || m.getResult() == null) continue;

            String homeId = m.getHomeTeam().getId();
            String awayId = m.getAwayTeam().getId();
            VolleyMatchResult res = m.getResult();

            int homePoints = res.getHomeTeamPoints() != null ? res.getHomeTeamPoints() : 0;
            int awayPoints = res.getAwayTeamPoints() != null ? res.getAwayTeamPoints() : 0;

            if (homeId.equals(teamA) && awayId.equals(teamB)) {
                // Porovnávame priamo získané body zo vzájomného zápasu (kto získal viac bodov 3b/2b, vyhráva)
                return Integer.compare(awayPoints, homePoints);
            }
            if (homeId.equals(teamB) && awayId.equals(teamA)) {
                return Integer.compare(homePoints, awayPoints);
            }
        }
        return 0;
    }
}
