package com.example.mauri.mapper;

import com.example.mauri.model.League;
import com.example.mauri.model.Team;
import com.example.mauri.model.dto.request.LeagueShortDTO;
import com.example.mauri.model.dto.response.PlayerResponseDTO;
import com.example.mauri.model.dto.response.TeamResponseDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.util.ParticipantNameUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TeamMapper {

    private final PlayerMapper playerMapper;
    private final LeagueRepository leagueRepository;


    public TeamResponseDTO mapToResponseDTO(Team team) {

        PlayerResponseDTO player1 = null;
        if (team.getPlayer1() != null) {
            player1 = playerMapper.mapToResponseDTO(team.getPlayer1());
        }

        PlayerResponseDTO player2 = null;
        if (team.getPlayer2() != null) {
            player2 = playerMapper.mapToResponseDTO(team.getPlayer2());
        }
        String teamName = ParticipantNameUtils.buildTeamName(team);

        List<League> leagues = leagueRepository.findLeaguesByTeamId(team.getId());

        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(teamName)
                .player1(player1)
                .player2(player2)
                .active(team.isActive())
                .deletedDate(team.getDeletedDate())
                .registrationDate(team.getCreatedAt())
                .leagues(leagues.stream()
                        .map(league -> new LeagueShortDTO(league.getId(),league.getName(),league.getSeason().getYear(),league.getLeagueType(),league.getStatus()))
                        .toList())
                .build();
    }
}
