package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.model.League;
import com.example.mauri.model.Season;
import com.example.mauri.model.dto.CreateSeasonDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.SeasonRepository;
import com.example.mauri.service.LeagueService;
import com.example.mauri.service.MatchService;
import com.example.mauri.service.SeasonService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeasonServiceBean implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueService leagueService;
    private final MatchService matchService;


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
                new ArrayList<>(),
                SeasonStatus.CREATED);
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

    @Override
    @Transactional
    public String startSeason(String seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("Sezóna s ID " + seasonId + " neexistuje."));

        if (season.getStatus() != SeasonStatus.CREATED) {
            throw new IllegalStateException("Sezónu možno spustiť len ak je v stave CREATED.");
        }

        List<League> leagues = leagueRepository.findAllBySeasonId(seasonId);

        for (League league : leagues) {
            LeagueStatus status = league.getStatus();

            if (status == LeagueStatus.ACTIVE || status == LeagueStatus.FINISHED) {
                continue;
            }

            if (status == LeagueStatus.CREATED) {
                try {
                    matchService.generateMatchesForLeague(league.getId());
                } catch (IllegalStateException e) {
                    System.out.println("Zápasy už existujú pre ligu: " + league.getName());
                }
            }
        }

        season.setStatus(SeasonStatus.ACTIVE);
        seasonRepository.save(season);

        return "Sezóna " + season.getYear() + " bola úspešne odštartovaná.";
    }
}
