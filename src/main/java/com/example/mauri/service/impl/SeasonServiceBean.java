package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.League;
import com.example.mauri.model.Season;
import com.example.mauri.model.dto.create.CreateSeasonDTO;
import com.example.mauri.model.dto.response.LeagueResponseDTO;
import com.example.mauri.model.dto.response.SeasonResponseDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.SeasonRepository;
import com.example.mauri.service.LeagueService;
import com.example.mauri.service.MatchService;
import com.example.mauri.service.SeasonService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonServiceBean implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueService leagueService;
    private final MatchService matchService;


    @Override
    public List<SeasonResponseDTO> getSeasons() {
        List<Season> seasons = seasonRepository.findAll();

        // Zoradíme podľa roku zostupne (najnovšia sezóna prvá)
        seasons.sort((s1, s2) -> Integer.compare(s2.getYear(), s1.getYear()));

        List<SeasonResponseDTO> seasonDTOs = new ArrayList<>();
        for (Season season : seasons) {
            seasonDTOs.add(mapSeasonToDTO(season, true));
        }

        return seasonDTOs;
    }

    @Override
    public SeasonResponseDTO getSeasonStats(String seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Season not found with id: " + seasonId));

        return mapSeasonToDTO(season, true);
    }

    @Override
    public SeasonResponseDTO createSeason(CreateSeasonDTO createSeasonDTO) {
        Season season = Season.builder()
                .id(UUID.randomUUID().toString())
                .year(createSeasonDTO.getYear())
                .build();
        season = seasonRepository.save(season);
        return mapSeasonToDTO(season, false);
    }


    @Transactional
    @Override
    public void deleteSeason(@NonNull String id) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No season found with id: " + id));

        List<League> leagues = season.getLeagues();

        for (League league : leagues) {
            leagueService.deleteLeagueById(league.getId());
        }
        seasonRepository.deleteById(id);
    }

    @Override
    @Transactional
    public String addLeagueToSeason(@NonNull String leagueId, @NonNull String seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("No season found with id: " + seasonId));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("No league found with id: " + leagueId));

        if (!season.getLeagues().contains(league)) {
            league.setSeason(season);
            season.getLeagues().add(league);
        }

        return "Liga " + league.getName() + " bola úspešne priradená k sezóne " + season.getYear() + ".";
    }

    @Override
    @Transactional
    public String startSeason(String seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Sezóna s ID " + seasonId + " neexistuje."));

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
        season.setStartDate(LocalDate.now());
        seasonRepository.save(season);

        return "Sezóna " + season.getYear() + " bola úspešne odštartovaná.";
    }

    @Override
    @Transactional
    public String finishSeason(String seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("No season found with id: " + seasonId));

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
        season.setEndDate(LocalDate.now());
        seasonRepository.save(season);
        return "Sezóna " + season.getYear() + " bola ukončená spolu s jej ligami.";
    }

    private SeasonResponseDTO mapSeasonToDTO(Season season, boolean includeLeagues) {
        List<LeagueResponseDTO> leagueDTOs = new ArrayList<>();
        long totalPlayers = 0;
        long totalTeams = 0;

        if (includeLeagues && season.getLeagues() != null) {
            for (League league : season.getLeagues()) {
                LeagueResponseDTO leagueDTO = mapLeagueToDTO(league);
                leagueDTOs.add(leagueDTO);
                totalPlayers += leagueDTO.getTotalPlayers();
                totalTeams += leagueDTO.getTotalTeams();
            }
        }

        return SeasonResponseDTO.builder()
                .id(season.getId())
                .year(season.getYear())
                .status(season.getStatus())
                .leagues(leagueDTOs)
                .totalPlayers(totalPlayers)
                .totalTeams(totalTeams)
                .createdAt(season.getCreatedAt())
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .build();
    }

    private LeagueResponseDTO mapLeagueToDTO(League league) {
        String id = league.getId();
        String name = league.getName();
        Integer year = league.getSeason() != null ? league.getSeason().getYear() : null;
        MatchType leagueType = league.getLeagueType();
        LeagueStatus status = league.getStatus();

        int playersCount = league.getPlayers() != null ? league.getPlayers().size() : 0;
        int teamsCount = league.getTeams() != null ? league.getTeams().size() : 0;

        String winner = null;
        if (status == LeagueStatus.FINISHED) {
            try {
                winner = leagueService.getLeagueWinnerName(id, leagueType);
            } catch (Exception e) {
                log.warn("Získanie víťaza ligy {} zlyhalo: {}", id, e.getMessage());
            }
        }
        return LeagueResponseDTO.builder()
                .leagueId(id)
                .leagueName(name)
                .seasonYear(year)
                .leagueType(leagueType)
                .leagueStatus(status)
                .totalPlayers(playersCount)
                .totalTeams(teamsCount)
                .winner(winner)
                .build();
    }
}
