package com.example.mauri.service.volley.impl;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.VolleyMatchMapper;
import com.example.mauri.model.Season;
import com.example.mauri.model.VolleyLeague;
import com.example.mauri.model.VolleyMatch;
import com.example.mauri.model.VolleyMatchResult;
import com.example.mauri.model.dto.create.CreateVolleyMatchDTO;
import com.example.mauri.model.dto.response.VolleyMatchResponseDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.volley.VolleyMatchActivityService;
import com.example.mauri.service.volley.VolleyMatchResultService;
import com.example.mauri.service.volley.VolleyMatchService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolleyMatchServiceBean implements VolleyMatchService {

    private final VolleyMatchRepository volleyMatchRepository;
    private final VolleyMatchMapper volleyMatchMapper;
    private final VolleyTeamRepository volleyTeamRepository;
    private final VolleyMatchResultService volleyMatchResultService;
    private final VolleyLeagueRepository volleyLeagueRepository;
    private final VolleyMatchActivityService volleyMatchActivityService;
    private final MatchActivityRepository matchActivityRepository;
    private final SeasonRepository seasonRepository;

    @Override
    public List<VolleyMatchResponseDTO> getMatches() {
        List<VolleyMatch> matches = volleyMatchRepository.findAll();
        return matches.stream()
                .map(volleyMatchMapper::toVolleyMatchResponse)
                .toList();
    }

    @Override
    public VolleyMatchResponseDTO createMatch(CreateVolleyMatchDTO createVolleyMatchDTO) {
        volleyLeagueRepository.findById(createVolleyMatchDTO.getVolleyLeagueId())
                .orElseThrow(() -> new ResourceNotFoundException("Volleyball league not found"));

        VolleyMatch match = VolleyMatch.builder()
                .id(UUID.randomUUID().toString())
                .volleyLeagueId(createVolleyMatchDTO.getVolleyLeagueId())
                .roundNumber(createVolleyMatchDTO.getRoundNumber())
                .build();

        match.setHomeTeam(volleyTeamRepository.findById(createVolleyMatchDTO.getHomeTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Home team not found")));
        match.setAwayTeam(volleyTeamRepository.findById(createVolleyMatchDTO.getAwayTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Away team not found")));

        match = volleyMatchRepository.save(match);
        log.info("Volleyball match {} created successfully", match.getId());
        return volleyMatchMapper.toVolleyMatchResponse(match);
    }

    @Override
    public void deleteMatch(String id) {
        if (!volleyMatchRepository.existsById(id)) {
            throw new ResourceNotFoundException("Match not found");
        }
        volleyMatchRepository.deleteById(id);
    }

    @Override
    public VolleyMatch addResult(String matchId, VolleyMatchResult matchResult) {
        VolleyMatch match = volleyMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        VolleyMatchResult finalResult = volleyMatchResultService.processResult(match, matchResult);
        match.setResult(finalResult);

        if (finalResult.getScratchedId() != null) {
            match.setStatus(MatchStatus.SCRATCHED);
        } else {
            match.setStatus(MatchStatus.FINISHED);
        }

        VolleyMatch savedMatch = volleyMatchRepository.save(match);
        volleyMatchActivityService.createActivity(savedMatch.getId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Volleyball match {} result added successfully by {}", savedMatch.getId(), username);

        return savedMatch;
    }

    @Override
    public Map<Integer, List<VolleyMatchResponseDTO>> getMatchesGroupedByRound(String leagueId) {
        List<VolleyMatch> matches = volleyMatchRepository.findByVolleyLeagueId(leagueId);
        return matches.stream()
                .map(volleyMatchMapper::toVolleyMatchResponse)
                .collect(Collectors.groupingBy(VolleyMatchResponseDTO::getRoundNumber));
    }

    @Override
    @Transactional
    public void cancelResult(String matchId) {
        VolleyMatch match = volleyMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        match.setResult(null);
        match.setStatus(MatchStatus.CREATED);
        volleyMatchRepository.save(match);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Volleyball match {} result cancelled successfully by {}", matchId, username);
        matchActivityRepository.deleteByMatchId(matchId);

    }

    @Override
    public List<VolleyMatchResponseDTO> getMatchesForVolleyTeamInActiveSeason(String teamId, MatchStatus status) {

        List<String> leagueIds = getActiveSeasonVolleyLeagueIds();
        if (leagueIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<VolleyMatch> matches = volleyMatchRepository.findByVolleyTeamStatusAndLeagueIds(teamId, status, leagueIds);
        return matches.stream()
                .map(volleyMatchMapper::toVolleyMatchResponse)
                .toList();
    }

    private List<String> getActiveSeasonVolleyLeagueIds() {
        Season activeSeason = seasonRepository.findByStatus(SeasonStatus.ACTIVE).orElse(null);

        if (activeSeason == null) {
            return new ArrayList<>();
        }
        List<String> leagueIds = new ArrayList<>();
        for (VolleyLeague league : activeSeason.getVolleyLeagues()) {
            leagueIds.add(league.getId());
        }
        return leagueIds;
    }
}
