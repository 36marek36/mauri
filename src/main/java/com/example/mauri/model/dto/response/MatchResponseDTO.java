package com.example.mauri.model.dto.response;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.model.MatchResult;
import com.example.mauri.model.dto.request.ParticipantDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponseDTO {
    private String id;
    private MatchType matchType;

    private ParticipantDTO homePlayer ;
    private ParticipantDTO awayPlayer ;

    private ParticipantDTO homeTeam ;
    private ParticipantDTO awayTeam ;

    private String leagueId;

    private MatchResult result;

    private Integer roundNumber;

    private MatchStatus status;

}
