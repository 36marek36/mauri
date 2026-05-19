package com.example.mauri.controller;

import com.example.mauri.model.dto.response.MatchActivityDTO;
import com.example.mauri.service.MatchActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
