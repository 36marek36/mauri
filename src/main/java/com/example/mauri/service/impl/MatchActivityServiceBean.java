package com.example.mauri.service.impl;

import com.example.mauri.mapper.MatchMapper;
import com.example.mauri.model.League;
import com.example.mauri.model.Match;
import com.example.mauri.model.MatchActivity;
import com.example.mauri.model.dto.response.MatchActivityDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchActivityRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.service.MatchActivityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchActivityServiceBean implements MatchActivityService {

    private final MatchActivityRepository matchActivityRepository;
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final LeagueRepository leagueRepository;

    @Override
    public void createActivity(String matchId) {

        MatchActivity activity = new MatchActivity();
        activity.setMatchId(matchId);
        activity.setCreatedAt(Instant.now());

        matchActivityRepository.save(activity);
    }

    @Override
    public List<MatchActivityDTO> getRecentActivities() {

        Instant threeDaysAgo =
                Instant.now().minus(3, ChronoUnit.DAYS);

        List<MatchActivity> activities =
                matchActivityRepository
                        .findByCreatedAtAfterOrderByCreatedAtDesc(threeDaysAgo);

        List<String> matchIds = activities.stream()
                .map(MatchActivity::getMatchId)
                .toList();

        List<Match> matches = matchRepository.findAllById(matchIds);

        Map<String, Match> matchMap = matches.stream()
                .collect(Collectors.toMap(
                        Match::getId,
                        Function.identity()
                ));

        // 👉 zber leagueId
        Set<String> leagueIds = matches.stream()
                .map(Match::getLeagueId)
                .collect(Collectors.toSet());

        // 👉 batch fetch líg
        Map<String, String> leagueNameMap = leagueRepository.findAllById(leagueIds)
                .stream()
                .collect(Collectors.toMap(
                        League::getId,
                        League::getName
                ));

        return activities.stream()
                .map(activity -> {

                    Match match = matchMap.get(activity.getMatchId());

                    if (match == null) {
                        return null;
                    }

                    MatchActivityDTO dto = new MatchActivityDTO();
                    dto.setMatch(matchMapper.mapMatchToDTO(match));
                    dto.setPlayedAt(activity.getCreatedAt());

                    dto.setLeagueName(
                            leagueNameMap.get(match.getLeagueId())
                    );

                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/Bratislava")
    @Transactional
    public void cleanupOldActivities() {
        log.info("Deleting old activities...");

        Instant limit =
                Instant.now().minus(4, ChronoUnit.DAYS);

        long deleted =
                matchActivityRepository
                        .deleteByCreatedAtBefore(limit);

        log.info("Deleted {} old activities", deleted);
    }
}
