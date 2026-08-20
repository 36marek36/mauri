package com.example.mauri.service.volley;

import com.example.mauri.model.dto.response.VolleyMatchActivityDTO;

import java.util.List;

public interface VolleyMatchActivityService {
    void createActivity(String matchId);
    List<VolleyMatchActivityDTO> getRecentActivities();
}
