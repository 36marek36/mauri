package com.example.mauri.service.volley.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.VolleyMatchMapper;
import com.example.mauri.model.VolleyMatch;
import com.example.mauri.model.VolleyMatchResult;
import com.example.mauri.model.dto.create.CreateVolleyMatchDTO;
import com.example.mauri.model.dto.response.VolleyMatchResponseDTO;
import com.example.mauri.repository.VolleyMatchRepository;
import com.example.mauri.repository.VolleyTeamRepository;
import com.example.mauri.service.volley.VolleyMatchResultService;
import com.example.mauri.service.volley.VolleyMatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolleyMatchServiceBean implements VolleyMatchService {

    private final VolleyMatchRepository volleyMatchRepository;
    private final VolleyMatchMapper volleyMatchMapper;
    private final VolleyTeamRepository volleyTeamRepository;
    private final VolleyMatchResultService volleyMatchResultService;

    @Override
    public List<VolleyMatchResponseDTO> getMatches() {
        List<VolleyMatch> matches = volleyMatchRepository.findAll();
        return matches.stream()
                .map(volleyMatchMapper::toVolleyMatchResponse)
                .toList();
    }

    @Override
    public VolleyMatchResponseDTO createMatch(CreateVolleyMatchDTO createVolleyMatchDTO) {
        VolleyMatch match = VolleyMatch.builder()
                .id(UUID.randomUUID().toString())
                .build();
        match.setHomeTeam(volleyTeamRepository.findById(createVolleyMatchDTO.getHomeTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Home team not found")));
        match.setAwayTeam(volleyTeamRepository.findById(createVolleyMatchDTO.getAwayTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Away team not found")));

        volleyMatchRepository.save(match);
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

        VolleyMatch savedMatch = volleyMatchRepository.save(match);

        return savedMatch;
    }
}
