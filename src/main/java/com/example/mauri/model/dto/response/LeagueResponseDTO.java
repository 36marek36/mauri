package com.example.mauri.model.dto.response;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.model.dto.request.ParticipantDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueResponseDTO {
    private String leagueId;
    private String leagueName;
    private Integer seasonYear;
    private MatchType leagueType;
    private LeagueStatus leagueStatus;
    private List<ParticipantDTO> players;
    private List<ParticipantDTO> teams;
    private List<String> droppedParticipantsIds;
    private String winner;
    private int leagueProgress;
}
