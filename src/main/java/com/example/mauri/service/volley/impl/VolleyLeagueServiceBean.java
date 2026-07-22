package com.example.mauri.service.volley.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.VolleyLeagueMapper;
import com.example.mauri.model.Season;
import com.example.mauri.model.VolleyLeague;
import com.example.mauri.model.dto.create.CreateVolleyLeagueDTO;
import com.example.mauri.model.dto.response.VolleyLeagueResponseDTO;
import com.example.mauri.repository.SeasonRepository;
import com.example.mauri.repository.VolleyLeagueRepository;
import com.example.mauri.service.volley.VolleyLeagueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolleyLeagueServiceBean implements VolleyLeagueService {

    private final VolleyLeagueRepository volleyLeagueRepository;
    private final VolleyLeagueMapper volleyLeagueMapper;
    private final SeasonRepository seasonRepository;

    @Override
    public List<VolleyLeagueResponseDTO> getLeagues() {
        List<VolleyLeague> leagues = volleyLeagueRepository.findAll();
        return leagues.stream()
                .map(volleyLeagueMapper::mapToResponseDTO)
                .toList();
    }

    @Override
    public VolleyLeagueResponseDTO createLeague(CreateVolleyLeagueDTO createVolleyLeagueDTO) {
        Season season = null;
        if (StringUtils.hasText(createVolleyLeagueDTO.getSeasonId())) {
            season = seasonRepository.findById(createVolleyLeagueDTO.getSeasonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Season not found"));
        }
        VolleyLeague volleyLeague = VolleyLeague.builder()
                .id(UUID.randomUUID().toString())
                .name(createVolleyLeagueDTO.getLeagueName())
                .season(season)
                .build();

        volleyLeague = volleyLeagueRepository.save(volleyLeague);
        log.info("Volleyball league {} created successfully", volleyLeague.getName());
        return volleyLeagueMapper.mapToResponseDTO(volleyLeague);
    }

    @Override
    public void deleteLeague(String id) {

    }
}
