package com.example.mauri.service.volley;

import com.example.mauri.model.dto.request.VolleyTeamPlayerNameRequest;
import com.example.mauri.model.dto.response.VolleyTeamResponseDTO;
import lombok.NonNull;

import java.util.List;

public interface VolleyTeamService {
    List<VolleyTeamResponseDTO> getActiveVolleyTeams();
    List<VolleyTeamResponseDTO> getInactiveVolleyTeams();
    VolleyTeamResponseDTO createVolleyTeam(String teamName, String captainId);
    String deleteVolleyTeam(String id);
    void addPlayerToTeam(String teamId, VolleyTeamPlayerNameRequest playerName);
    void removePlayerFromTeam(String teamId, VolleyTeamPlayerNameRequest playerName);
    void deactivateTeam(@NonNull String id);
}
