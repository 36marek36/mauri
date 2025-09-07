package com.example.mauri.mapper;

import com.example.mauri.model.Team;
import com.example.mauri.model.dto.request.ParticipantDTO;
import com.example.mauri.model.dto.response.TeamResponseDTO;
import com.example.mauri.util.ParticipantNameUtils;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {
    public TeamResponseDTO mapToResponseDTO (Team team){

        ParticipantDTO player1 = null;
        if (team.getPlayer1() != null) {
            String name1 = ParticipantNameUtils.buildPlayerName(team.getPlayer1());
            player1 = new ParticipantDTO(team.getPlayer1().getId(), name1,team.getPlayer1().isActive());
        }

        ParticipantDTO player2 = null;
        if (team.getPlayer2() != null) {
            String name2 = ParticipantNameUtils.buildPlayerName(team.getPlayer2());
            player2 = new ParticipantDTO(team.getPlayer2().getId(), name2,team.getPlayer2().isActive());
        }
        String teamName = ParticipantNameUtils.buildTeamName(team);

        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(teamName)
                .player1(player1)
                .player2(player2)
                .active(team.isActive())
                .deletedDate(team.getDeletedDate())
                .registrationDate(team.getCreatedAt())
                .build();
    }
}
