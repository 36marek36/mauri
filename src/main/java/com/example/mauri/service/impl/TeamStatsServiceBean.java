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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamStatsServiceBean implements TeamStatsService {

    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final TeamService teamService;

    // Získanie štatistík jedného tímu v lige
    @Override
    public TeamStatsDTO getTeamStats(String leagueId, String teamId) {
        // Načítame všetky ukončené zápasy ligy naraz (pre lepšiu efektivitu)
        List<Match> matches = matchRepository.findByLeagueIdAndStatus(leagueId, MatchStatus.FINISHED);

        Team team = teamService.getTeamById(teamId);
        return calculateTeamStats(team, matches,leagueId);
    }

    // Získanie štatistík všetkých tímov v lige
    @Override
    public List<TeamStatsDTO> getAllStatsForLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));

        List<Team> teams = league.getTeams();

        // Načítame všetky ukončené zápasy ligy naraz
        List<Match> matches = matchRepository.findByLeagueIdAndStatus(leagueId, MatchStatus.FINISHED);

        List<TeamStatsDTO> statsList = new ArrayList<>();

        for (Team team : teams) {
            TeamStatsDTO stats = calculateTeamStats(team, matches,leagueId);
            statsList.add(stats);
        }

        statsList.sort((a, b) -> {
            // Najprv podľa počtu vyhraných setov zostupne
            int cmp = Integer.compare(b.getSetsWon(), a.getSetsWon());
            if (cmp != 0) return cmp;

            // Ak rovnaké, podľa vzájomných zápasov
            return compareHeadToHeadInMemory(a.getTeamId(), b.getTeamId(), matches);
        });

        return statsList;
    }

    @Override
    public int teamProgress(String leagueId, String teamId) {
        int played = matchRepository.countPlayedMatchesByTeam(leagueId,teamId,MatchStatus.FINISHED);
        int total = matchRepository.countTotalMatchesByTeam(leagueId,teamId);

        if (total == 0) return 0;

        return (int) ((double) played / total * 100);
    }

    // Privátna metóda na výpočet štatistík tímu zo zoznamu zápasov
    private TeamStatsDTO calculateTeamStats(Team team, List<Match> matches, String leagueId) {
        String teamId = team.getId();
        String teamName = ParticipantNameUtils.buildTeamName(team);

        int matchesPlayed = 0;
        int wins = 0;
        int losses = 0;
        int setsWon = 0;
        int setsLost = 0;

        for (Match match : matches) {
            boolean isHome = match.getHomeTeam() != null && match.getHomeTeam().getId().equals(teamId);
            boolean isAway = match.getAwayTeam() != null && match.getAwayTeam().getId().equals(teamId);
            if (!isHome && !isAway) continue;

            MatchResult result = match.getResult();
            if (result == null) continue;

            int teamSets = isHome ? result.getScore1() : result.getScore2();
            int opponentSets = isHome ? result.getScore2() : result.getScore1();

            matchesPlayed++;
            setsWon += teamSets;
            setsLost += opponentSets;

            if (teamId.equals(result.getWinnerId())) wins++;
            else losses++;
        }

        int progress = teamProgress(leagueId,teamId);

        return TeamStatsDTO.builder()
                .teamId(teamId)
                .teamName(teamName)
                .matches(matchesPlayed)
                .wins(wins)
                .losses(losses)
                .setsWon(setsWon)
                .setsLost(setsLost)
                .leagueProgress(progress)
                .build();
    }

    private int compareHeadToHeadInMemory(String teamId1, String teamId2, List<Match> allMatches) {
        int winsTeam1 = 0;
        int winsTeam2 = 0;

        for (Match match : allMatches) {
            if (match.getHomeTeam() == null || match.getAwayTeam() == null) continue;

            boolean isBetweenTeams =
                    (match.getHomeTeam().getId().equals(teamId1) && match.getAwayTeam().getId().equals(teamId2)) ||
                            (match.getHomeTeam().getId().equals(teamId2) && match.getAwayTeam().getId().equals(teamId1));

            if (!isBetweenTeams) continue;

            MatchResult result = match.getResult();
            if (result == null) continue;

            String winnerId = result.getWinnerId();
            if (winnerId.equals(teamId1)) winsTeam1++;
            else if (winnerId.equals(teamId2)) winsTeam2++;
        }

        if (winsTeam1 > winsTeam2) return -1;  // teamId1 lepší
        else if (winsTeam2 > winsTeam1) return 1; // teamId2 lepší
        else return 0; // nerozhodné
    }
}

//    public TeamStatsDTO getTeamStats(String leagueId, String teamId) {
//
//        List<Match> matches = matchRepository.findByLeagueIdAndTeam(leagueId, teamId);
//
//        Team team = teamService.getTeamById(teamId);
//        String teamName = ParticipantNameUtils.buildTeamName(team);
/// /        String teamName = team.getPlayer1().getLastName()+" / "+team.getPlayer2().getLastName();
//
//
//        int matchesPlayed = 0;
//        int wins = 0;
//        int losses = 0;
//        int setsWon = 0;
//        int setsLost = 0;
//
//        for (Match match : matches) {
//            if (match.getStatus() != MatchStatus.FINISHED || match.getResult() == null) {
//                continue;
//            }
//
//            MatchResult matchResult = match.getResult();
//
//            boolean isHome = match.getHomeTeam().getId().equals(teamId);
//            int teamSets = isHome ? matchResult.getScore1() : matchResult.getScore2();
//            int opponentSets = isHome ? matchResult.getScore2() : matchResult.getScore1();
//
//            matchesPlayed++;
//            setsWon += teamSets;
//            setsLost += opponentSets;
//
//            if (matchResult.getWinnerId().equals(teamId)) {
//                wins++;
//            } else {
//                losses++;
//            }
//        }
//        return TeamStatsDTO.builder()
//                .teamId(teamId)
//                .teamName(teamName)
//                .matches(matchesPlayed)
//                .wins(wins)
//                .losses(losses)
//                .setsWon(setsWon)
//                .setsLost(setsLost)
//                .build();
//    }
//
//    public List<TeamStatsDTO> getAllStatsForLeague(String leagueId) {
//        League league = leagueRepository.findById(leagueId)
//                .orElseThrow(() -> new ResourceNotFoundException("League not found"));
//
//
//        List<Team> teams = league.getTeams();
//        List<TeamStatsDTO> statsList = new ArrayList<>();
//
//        for (Team team : teams) {
//            TeamStatsDTO stats = getTeamStats(leagueId, team.getId());
//            statsList.add(stats);
//        }
//
//        statsList.sort(Comparator.comparingInt(TeamStatsDTO::getSetsWon).reversed());
//        return statsList;
//    }
//}
