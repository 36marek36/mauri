package com.example.mauri.service.volley.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.VolleyLeagueMapper;
import com.example.mauri.model.Season;
import com.example.mauri.model.VolleyLeague;
import com.example.mauri.model.VolleyMatch;
import com.example.mauri.model.VolleyTeam;
import com.example.mauri.model.dto.create.CreateVolleyLeagueDTO;
import com.example.mauri.model.dto.response.VolleyLeagueResponseDTO;
import com.example.mauri.repository.SeasonRepository;
import com.example.mauri.repository.VolleyLeagueRepository;
import com.example.mauri.repository.VolleyMatchRepository;
import com.example.mauri.repository.VolleyTeamRepository;
import com.example.mauri.service.volley.VolleyLeagueService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final VolleyTeamRepository volleyTeamRepository;
    private final VolleyMatchRepository volleyMatchRepository;

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
    @Transactional
    public void deleteLeague(String id) {
        VolleyLeague volleyLeague = volleyLeagueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Volleyball league not found"));
        volleyLeagueRepository.delete(volleyLeague);
        log.info("Volleyball league {} deleted successfully", volleyLeague.getName());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public String addTeamsToLeague(String leagueId, List<String> teamIds) {
        VolleyLeague league = volleyLeagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("Volleyball league not found"));
        List<VolleyTeam> teams = volleyTeamRepository.findAllById(teamIds);
        league.getTeams().addAll(teams);
        volleyLeagueRepository.save(league);
        return "Tímy boli úspešne pridané do ligy";
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public String removeTeamFromLeague(String leagueId, String teamId) {
        VolleyLeague league = volleyLeagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("Volleyball league not found"));
        VolleyTeam team = volleyTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Volleyball team not found"));

        List<VolleyMatch> affectedMatches = volleyMatchRepository.findMatchesByLeagueAndTeam(leagueId, teamId);

        volleyMatchRepository.deleteAll(affectedMatches);

        league.getTeams().remove(team);

        return "Tím bol z ligy úspešne odstránený";
    }

    @Override
    public String startLeague(String leagueId) {
        VolleyLeague league = volleyLeagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("Volleyball league not found"));

        if (league.getStatus().equals(LeagueStatus.ACTIVE)) {
            throw new IllegalStateException("League is already active");
        }

        league.setStatus(LeagueStatus.ACTIVE);
        volleyLeagueRepository.save(league);
        return "Liga bola uspešne spustená";
    }

    @Override
    public String finishLeague(String leagueId) {
        VolleyLeague league = volleyLeagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("Volleyball league not found"));
        if (league.getStatus().equals(LeagueStatus.FINISHED)) {
            throw new IllegalStateException("League is already finished");
        }
        league.setStatus(LeagueStatus.FINISHED);
        volleyLeagueRepository.save(league);

        List<VolleyMatch> matches = volleyMatchRepository.findByVolleyLeagueId(leagueId);
        for (VolleyMatch match : matches) {
            if (match.getStatus() == MatchStatus.CREATED) {
                match.setStatus(MatchStatus.CANCELLED);
            }
        }
        volleyMatchRepository.saveAll(matches);
        return "Liga bola uspešne ukončená";
    }
}
