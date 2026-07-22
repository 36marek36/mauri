package com.example.mauri.service.volley;

import com.example.mauri.model.VolleyMatch;
import com.example.mauri.model.VolleyMatchResult;
import com.example.mauri.model.dto.create.CreateVolleyMatchDTO;
import com.example.mauri.model.dto.response.VolleyMatchResponseDTO;

import java.util.List;

public interface VolleyMatchService {
    List<VolleyMatchResponseDTO> getMatches();
    VolleyMatchResponseDTO createMatch(CreateVolleyMatchDTO createVolleyMatchDTO);
    void deleteMatch(String id);
    VolleyMatch addResult (String matchId, VolleyMatchResult matchResult);
}
