package com.example.mauri.service.volley;

import com.example.mauri.model.dto.request.VolleyTeamPlayerNameRequest;
import com.example.mauri.model.dto.response.VolleyTeamResponseDTO;

import java.util.List;

public interface VolleyTeamService {
    List<VolleyTeamResponseDTO> getVolleyTeams();
    VolleyTeamResponseDTO createVolleyTeam(String teamName, String captainId);
    void deleteVolleyTeam(String id);
    void addPlayerToTeam(String teamId, VolleyTeamPlayerNameRequest playerName);
    void removePlayerFromTeam(String teamId, VolleyTeamPlayerNameRequest playerName);
}
