package com.example.mauri.service;

import com.example.mauri.model.dto.response.MatchActivityDTO;

import java.util.List;

public interface MatchActivityService {
    void createActivity(String matchId);
    List<MatchActivityDTO> getRecentActivities();
    void cleanupOldActivities();
}
