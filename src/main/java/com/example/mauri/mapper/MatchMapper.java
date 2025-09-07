package com.example.mauri.mapper;

import com.example.mauri.model.Match;
import com.example.mauri.model.dto.request.ParticipantDTO;
import com.example.mauri.model.dto.response.MatchResponseDTO;
import com.example.mauri.service.PlayerStatsService;
import com.example.mauri.service.TeamStatsService;
import com.example.mauri.util.ParticipantNameUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MatchMapper {

    private final PlayerStatsService playerStatsService;
    private final TeamStatsService teamStatsService;


    public MatchResponseDTO mapMatchToDTO(Match match) {
        ParticipantDTO homePlayer = null;
        ParticipantDTO awayPlayer = null;
        ParticipantDTO homeTeam = null;
        ParticipantDTO awayTeam = null;
        switch (match.getMatchType()) {
            case SINGLES -> {
                if (match.getHomePlayer() != null) {
                    String name = ParticipantNameUtils.buildPlayerName(match.getHomePlayer());
                    int progress = playerStatsService.playerProgress(match.getLeagueId(), match.getHomePlayer().getId());
                    homePlayer = new ParticipantDTO(match.getHomePlayer().getId(), name,match.getHomePlayer().isActive(),progress);
                }
                if (match.getAwayPlayer() != null) {
                    String name = ParticipantNameUtils.buildPlayerName(match.getAwayPlayer());
                    int progress = playerStatsService.playerProgress(match.getLeagueId(), match.getAwayPlayer().getId());
                    awayPlayer = new ParticipantDTO(match.getAwayPlayer().getId(), name,match.getAwayPlayer().isActive(),progress);
                }
            }
            case DOUBLES -> {
                if (match.getHomeTeam() != null) {
                    String teamName = ParticipantNameUtils.buildTeamName(match.getHomeTeam());
                    int progress = teamStatsService.teamProgress(match.getLeagueId(), match.getHomeTeam().getId());
                    homeTeam = new ParticipantDTO(match.getHomeTeam().getId(), teamName,match.getHomeTeam().isActive(),progress);
                }
                if (match.getAwayTeam() != null) {
                    String teamName = ParticipantNameUtils.buildTeamName(match.getAwayTeam());
                    int progress = teamStatsService.teamProgress(match.getLeagueId(), match.getAwayTeam().getId());
                    awayTeam = new ParticipantDTO(match.getAwayTeam().getId(), teamName,match.getAwayTeam().isActive(),progress);
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
