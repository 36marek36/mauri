package com.example.mauri.service;

import com.example.mauri.model.League;
import com.example.mauri.model.Season;
import com.example.mauri.model.dto.CreateSeasonDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.SeasonRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SeasonServiceBean implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueService leagueService;

    public SeasonServiceBean(SeasonRepository seasonRepository, LeagueRepository leagueRepository, LeagueService leagueService) {
        this.seasonRepository = seasonRepository;
        this.leagueRepository = leagueRepository;
        this.leagueService = leagueService;
    }

    @Override
    public List<Season> getSeasons() {
        return seasonRepository.findAll().stream().toList();
    }

    @Override
    public Season getSeason(@NonNull String id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No season found with id: " + id));
    }

    @Override
    public Season createSeason(CreateSeasonDTO createSeasonDTO) {
        Season season = new Season(
                UUID.randomUUID().toString(),
                createSeasonDTO.getYear(),
                new ArrayList<>());
        return seasonRepository.save(season);
    }

    @Transactional
    @Override
    public void deleteSeason(@NonNull String id) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No season found with id: " + id));

        List<League> leagues = season.getLeagues();

        for (League league : leagues) {
            leagueService.deleteLeagueById(league.getId());
        }
        seasonRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Season addLeagueToSeason(@NonNull String leagueId, @NonNull String seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("No season found with id: " + seasonId));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("No league found with id: " + leagueId));

        if (!season.getLeagues().contains(league)) {
            league.setSeason(season);              // nastavíme väzbu
            season.getLeagues().add(league);       // bidirectional
        }

        return season;
    }

}
