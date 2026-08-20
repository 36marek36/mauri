package com.example.mauri.controller.volley;

import com.example.mauri.model.dto.response.VolleyMatchActivityDTO;
import com.example.mauri.service.volley.VolleyMatchActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/volleyball/match-activities")
@RequiredArgsConstructor
public class VolleyMatchActivityController {
    private final VolleyMatchActivityService volleyMatchActivityService;

    @RequestMapping("/recent")
    public ResponseEntity<List<VolleyMatchActivityDTO>> getRecentActivities() {
        return ResponseEntity.ok(volleyMatchActivityService.getRecentActivities());
    }
}
