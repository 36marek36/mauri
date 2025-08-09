package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.model.League;
import com.example.mauri.model.Season;
import com.example.mauri.model.dto.CreateSeasonDTO;
import com.example.mauri.model.dto.LeagueDTO;
import com.example.mauri.model.dto.SeasonDTO;
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
    public List<SeasonDTO> getSeasons() {
        List<Season> seasons = seasonRepository.findAll();
        List<SeasonDTO> seasonDTOs = new ArrayList<>();

        for (Season season : seasons) {
            List<LeagueDTO> leagueDTOs = new ArrayList<>();
            long totalPlayers = 0;
            long totalTeams = 0;

            for (League league : season.getLeagues()) {
                int playersCount = league.getPlayers() != null ? league.getPlayers().size() : 0;
                int teamsCount = league.getTeams() != null ? league.getTeams().size() : 0;

                totalPlayers += playersCount;
                totalTeams += teamsCount;

                String winner = null;

                if (league.getStatus() == LeagueStatus.FINISHED) {
                    winner = leagueService.getLeagueWinnerName(league.getId(), league.getLeagueType());
                }

                leagueDTOs.add(new LeagueDTO(
                        league.getId(),
                        league.getName(),
                        season.getYear(),
                        league.getLeagueType(),
                        league.getStatus(),
                        playersCount,
                        teamsCount,
                        winner
                ));
            }

            seasonDTOs.add(new SeasonDTO(
                    season.getId(),
                    season.getYear(),
                    season.getStatus(),
                    leagueDTOs,
                    totalPlayers,
                    totalTeams
            ));
        }

        return seasonDTOs;
    }

//    @Override
//    public Season getSeason(@NonNull String id) {
//        return seasonRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("No season found with id: " + id));
//    }

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
            if (season.getStatus() == SeasonStatus.ACTIVE) {
                throw new IllegalStateException("Sezóna " + season.getYear() + " je už spustená.");
            }
            if (season.getStatus() == SeasonStatus.FINISHED) {
                throw new IllegalStateException("Sezóna " + season.getYear() + " bola už ukončená.");
            }
            throw new IllegalStateException("Sezónu možno spustiť len ak je v stave CREATED.");
        }

        List<League> leagues = leagueRepository.findAllBySeasonId(seasonId);

        if (leagues.isEmpty()) {
            throw new IllegalStateException("Sezóna nemá žiadne ligy. Nie je možné ju spustiť.");
        }

        for (League league : leagues) {
            LeagueStatus status = league.getStatus();

            if (status == LeagueStatus.ACTIVE || status == LeagueStatus.FINISHED) {
                continue;
            }

            if (status == LeagueStatus.CREATED) {
               matchService.generateMatchesForLeague(league.getId());
            }
        }

        season.setStatus(SeasonStatus.ACTIVE);
        seasonRepository.save(season);

        return "Sezóna " + season.getYear() + " bola úspešne odštartovaná.";
    }

    @Override
    @Transactional
    public String finishSeason(String seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("No season found with id: " + seasonId));

        if (season.getStatus() == SeasonStatus.FINISHED) {
            throw new IllegalStateException("Season already finished");
        }

        List<League> leagues = leagueRepository.findAllBySeasonId(seasonId);

        for (League league : leagues) {
            // Preskočí už ukončené ligy
            if (league.getStatus() != LeagueStatus.FINISHED) {
                leagueService.finishLeague(league.getId());
            }
        }

        season.setStatus(SeasonStatus.FINISHED);
        seasonRepository.save(season);
        return "Sezóna " + season.getYear() + " bola ukončená spolu s jej ligami.";
    }

    @Override
    public SeasonDTO getSeasonStats(String seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new RuntimeException("Season not found with id: " + seasonId));

        long totalPlayers = leagueRepository.countPlayersBySeasonId(seasonId);
        long totalTeams = leagueRepository.countTeamsBySeasonId(seasonId);

        List<LeagueDTO> leagueDTOs = new ArrayList<>();

        for (League league : season.getLeagues()) {
            String id = league.getId();
            String name = league.getName();
            Integer year = null;
            if (league.getSeason() != null) {
                year = league.getSeason().getYear();
            }
            MatchType leagueType = league.getLeagueType();
            LeagueStatus status = league.getStatus();

            int playersCount = (league.getPlayers() != null) ? league.getPlayers().size() : 0;
            int teamsCount = (league.getTeams() != null) ? league.getTeams().size() : 0;

            String winner = null;

            if (status == LeagueStatus.FINISHED) {
                winner = leagueService.getLeagueWinnerName(id, league.getLeagueType());
            }

            LeagueDTO dto = new LeagueDTO(id, name, year, leagueType, status,playersCount, teamsCount,winner);
            leagueDTOs.add(dto);
        }

        return new SeasonDTO(season.getId(), season.getYear(),season.getStatus(),leagueDTOs, totalPlayers, totalTeams);
    }
}
