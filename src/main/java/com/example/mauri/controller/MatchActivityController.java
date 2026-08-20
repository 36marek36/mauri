package com.example.mauri.controller;

import com.example.mauri.model.dto.response.MatchActivityDTO;
import com.example.mauri.service.MatchActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest/match-activities")
@RequiredArgsConstructor
public class MatchActivityController {

    private final MatchActivityService matchActivityService;

    @GetMapping("/recent")
    public ResponseEntity<List<MatchActivityDTO>> getRecentActivities() {
        return ResponseEntity.ok(
                matchActivityService.getRecentActivities()
        );
    }
    @PatchMapping("/migrate")
    public ResponseEntity<Map<String, Object>> migrate() {
        long updatedCount = matchActivityService.migrateOldActivitiesToTennis();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Staré aktivity boli úspešne zmigrované.");
        response.put("updatedRecords", updatedCount);

        return ResponseEntity.ok(response);
    }
}
