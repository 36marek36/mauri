package com.example.mauri.mapper;

import com.example.mauri.model.Match;
import com.example.mauri.model.dto.request.ParticipantDTO;
import com.example.mauri.model.dto.response.MatchResponseDTO;
import com.example.mauri.util.ParticipantNameUtils;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

    public MatchResponseDTO mapMatchToDTO(Match match) {
        ParticipantDTO homePlayer = null;
        ParticipantDTO awayPlayer = null;
        ParticipantDTO homeTeam = null;
        ParticipantDTO awayTeam = null;
        switch (match.getMatchType()) {
            case SINGLES -> {
                if (match.getHomePlayer() != null) {
                    String name = ParticipantNameUtils.buildPlayerName(match.getHomePlayer());
                    homePlayer = new ParticipantDTO(match.getHomePlayer().getId(), name,match.getHomePlayer().isActive());
                }
                if (match.getAwayPlayer() != null) {
                    String name = ParticipantNameUtils.buildPlayerName(match.getAwayPlayer());
                    awayPlayer = new ParticipantDTO(match.getAwayPlayer().getId(), name,match.getAwayPlayer().isActive());
                }
            }
            case DOUBLES -> {
                if (match.getHomeTeam() != null) {
                    String teamName = ParticipantNameUtils.buildTeamName(match.getHomeTeam());
                    homeTeam = new ParticipantDTO(match.getHomeTeam().getId(), teamName,match.getHomeTeam().isActive());
                }
                if (match.getAwayTeam() != null) {
                    String teamName = ParticipantNameUtils.buildTeamName(match.getAwayTeam());
                    awayTeam = new ParticipantDTO(match.getAwayTeam().getId(), teamName,match.getAwayTeam().isActive());
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
