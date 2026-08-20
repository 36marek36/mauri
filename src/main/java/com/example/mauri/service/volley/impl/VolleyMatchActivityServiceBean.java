package com.example.mauri.service.volley.impl;

import com.example.mauri.enums.Sport;
import com.example.mauri.mapper.VolleyMatchMapper;
import com.example.mauri.model.MatchActivity;
import com.example.mauri.model.VolleyLeague;
import com.example.mauri.model.VolleyMatch;
import com.example.mauri.model.dto.response.VolleyMatchActivityDTO;
import com.example.mauri.repository.MatchActivityRepository;
import com.example.mauri.repository.VolleyLeagueRepository;
import com.example.mauri.repository.VolleyMatchRepository;
import com.example.mauri.service.volley.VolleyMatchActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolleyMatchActivityServiceBean implements VolleyMatchActivityService {

    private final MatchActivityRepository matchActivityRepository;
    private final VolleyMatchRepository volleyMatchRepository;
    private final VolleyLeagueRepository volleyLeagueRepository;
    private final VolleyMatchMapper volleyMatchMapper;

    @Override
    public void createActivity(String matchId) {
        MatchActivity activity = new MatchActivity();
        activity.setMatchId(matchId);
        activity.setSport(Sport.VOLLEYBALL);
        activity.setCreatedAt(Instant.now());

        matchActivityRepository.save(activity);
    }

    @Override
    public List<VolleyMatchActivityDTO> getRecentActivities() {
        Instant fiveDaysAgo = Instant.now().minus(5, java.time.temporal.ChronoUnit.DAYS);

        List<MatchActivity> activities = matchActivityRepository.findBySportAndCreatedAtAfterOrderByCreatedAtDesc(Sport.VOLLEYBALL, fiveDaysAgo);
        List<String> matchIds = activities.stream().map(MatchActivity::getMatchId).toList();
        List<VolleyMatch> matches = volleyMatchRepository.findAllById(matchIds);

        Map<String, VolleyMatch> matchMap = matches.stream()
                .collect(Collectors.toMap(VolleyMatch::getId, Function.identity()));
        Set<String> leagueIds = matches.stream().map(VolleyMatch::getVolleyLeagueId).collect(Collectors.toSet());
        Map<String, String> leagueNameMap = volleyLeagueRepository.findAllById(leagueIds).stream()
                .collect(Collectors.toMap(VolleyLeague::getId, VolleyLeague::getName));
        return activities.stream()
                .map(activity -> {
                    VolleyMatch match = matchMap.get(activity.getMatchId());
                    if (match == null) {
                        return null;
                    }
                    VolleyMatchActivityDTO dto = new VolleyMatchActivityDTO();
                    dto.setMatch(volleyMatchMapper.toVolleyMatchResponse(match));
                    dto.setPlayedAt(activity.getCreatedAt());
                    dto.setLeagueName(leagueNameMap.get(match.getVolleyLeagueId()));
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
