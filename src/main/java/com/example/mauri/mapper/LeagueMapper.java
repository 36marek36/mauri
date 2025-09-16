package com.example.mauri.mapper;

import com.example.mauri.model.League;
import com.example.mauri.model.dto.response.LeagueResponseDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
@Slf4j
public class LeagueMapper {

    public LeagueResponseDTO mapLeagueToDTO(League league) {
        return LeagueResponseDTO.builder()
                .leagueId(league.getId())
                .leagueName(league.getName())
                .seasonYear(league.getSeason() != null ? league.getSeason().getYear() : null)
                .leagueType(league.getLeagueType())
                .leagueStatus(league.getStatus())
                .droppedParticipantsIds(league.getDroppedParticipantsIds())
                .build();
    }

}
