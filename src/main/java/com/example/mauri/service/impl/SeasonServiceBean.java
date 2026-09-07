package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.SeasonMapper;
import com.example.mauri.model.League;
import com.example.mauri.model.Match;
import com.example.mauri.model.Season;
import com.example.mauri.model.VolleyLeague;
import com.example.mauri.model.dto.create.CreateSeasonDTO;
import com.example.mauri.model.dto.request.SeasonShortDTO;
import com.example.mauri.model.dto.response.SeasonResponseDTO;
import com.example.mauri.model.dto.response.TennisSeasonListResponseDTO;
import com.example.mauri.model.dto.response.VolleySeasonListResponseDTO;
import com.example.mauri.model.dto.update.UpdateSeasonDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.repository.SeasonRepository;
import com.example.mauri.repository.VolleyLeagueRepository;
import com.example.mauri.service.LeagueService;
import com.example.mauri.service.MatchService;
import com.example.mauri.service.SeasonService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonServiceBean implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueService leagueService;
    private final MatchService matchService;
    private final SeasonMapper seasonMapper;
    private final MatchRepository matchRepository;
    private final VolleyLeagueRepository volleyLeagueRepository;


    @Override
    public List<SeasonResponseDTO> getSeasons() {
        List<Season> seasons = seasonRepository.findAll();

        // Zoradíme podľa roku zostupne (najnovšia sezóna prvá)
        seasons.sort((s1, s2) -> Integer.compare(s2.getYear(), s1.getYear()));

        List<SeasonResponseDTO> seasonDTOs = new ArrayList<>();
        for (Season season : seasons) {
            seasonDTOs.add(seasonMapper.mapSeasonToDTO(season, true));
        }

        return seasonDTOs;
    }

    @Override
    public SeasonResponseDTO getSeasonStats(String seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Season not found with id: " + seasonId));

        return seasonMapper.mapSeasonToDTO(season, true);
    }

//    @Override
//    public SeasonResponseDTO getCurrentSeason() {
//        return seasonRepository.findByStatus(SeasonStatus.ACTIVE)
//                .map(season -> seasonMapper.mapSeasonToDTO(season, true))
//                .orElseThrow(() -> new ResourceNotFoundException("Žiadna aktuálna sezóna nie je dostupná."));
//    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public SeasonResponseDTO updateSeason(String seasonId, UpdateSeasonDTO updateSeasonDTO) {
        Season existingSeason = getSeasonOrThrow(seasonId);

        if (updateSeasonDTO.getYear() != null) {
            existingSeason.setYear(updateSeasonDTO.getYear());
        }

        if (updateSeasonDTO.getStatus() != null) {
            existingSeason.setStatus(updateSeasonDTO.getStatus());
        }

        if (updateSeasonDTO.getStartDate() != null) {
            existingSeason.setStartDate(updateSeasonDTO.getStartDate());
        }

        if (updateSeasonDTO.getEndDate() != null) {
            existingSeason.setEndDate(updateSeasonDTO.getEndDate());
        }
        // ✅ validácia po aplikovaní zmien
        if (existingSeason.getStartDate() != null && existingSeason.getEndDate() != null) {
            if (existingSeason.getEndDate().isBefore(existingSeason.getStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
        }
        Season saved = seasonRepository.save(existingSeason);
        return seasonMapper.mapSeasonToDTO(saved, false);
    }

    @Override
    public boolean isSeasonActive() {
        return seasonRepository.findByStatus(SeasonStatus.ACTIVE).isPresent();
    }

    @Override
    public SeasonShortDTO getCurrentSeasonShort() {
        return seasonRepository.findByStatus(SeasonStatus.ACTIVE)
                .map(season -> SeasonShortDTO.builder()
                        .id(season.getId())
                        .year(season.getYear())
                        .status(season.getStatus())
                        .build())
                .orElse(null);
    }

    @Override
    @Transactional
    public List<TennisSeasonListResponseDTO> getTennisSeasons(List<SeasonStatus> statuses) {

        List<Season> seasons;

        if (statuses == null || statuses.isEmpty()) {
            seasons = seasonRepository.findAllByOrderByYearDesc();
        } else {
            seasons = seasonRepository.findByStatusInOrderByYearDesc(statuses);
        }

        if (seasons.isEmpty()) {
            return List.of();
        }

        Set<String> seasonIds = seasons.stream()
                .map(Season::getId)
                .collect(Collectors.toSet());

        List<League> leagues =
                leagueRepository.findBySeasonIdIn(seasonIds);

        Set<String> leagueIds = leagues.stream()
                .map(League::getId)
                .collect(Collectors.toSet());

        List<Match> matches =
                leagueIds.isEmpty()
                        ? List.of()
                        : matchRepository.findByLeagueIdIn(leagueIds);

        Map<String, List<League>> leaguesBySeason =
                leagues.stream()
                        .collect(Collectors.groupingBy(
                                league -> league.getSeason().getId()
                        ));

        Map<String, List<Match>> matchesByLeague =
                matches.stream()
                        .collect(Collectors.groupingBy(
                                Match::getLeagueId
                        ));

        return seasons.stream()
                .map(season -> {

                    List<League> seasonLeagues =
                            leaguesBySeason.getOrDefault(
                                    season.getId(),
                                    List.of()
                            );

                    Set<String> participantIds = new HashSet<>();

                    long totalPlayers = 0;
                    long totalTeams = 0;
                    long totalMatches = 0;
                    long totalFinishedMatches = 0;
                    long totalScratchedMatches = 0;
                    long totalCancelledMatches = 0;
                    long totalCompletedMatches = 0;

                    for (League league : seasonLeagues) {

                        // Singles
                        if (league.getPlayers() != null) {
                            totalPlayers += league.getPlayers().size();

                            league.getPlayers().forEach(player ->
                                    participantIds.add(player.getId())
                            );
                        }

                        // Doubles
                        if (league.getTeams() != null) {
                            totalTeams += league.getTeams().size();

                            league.getTeams().forEach(team -> {

                                if (team.getPlayer1() != null) {
                                    participantIds.add(
                                            team.getPlayer1().getId()
                                    );
                                }

                                if (team.getPlayer2() != null) {
                                    participantIds.add(
                                            team.getPlayer2().getId()
                                    );
                                }
                            });
                        }

                        // Matches
                        List<Match> leagueMatches =
                                matchesByLeague.getOrDefault(
                                        league.getId(),
                                        List.of()
                                );

                        totalMatches += leagueMatches.size();

                        totalFinishedMatches += leagueMatches.stream()
                                .filter(match -> match.getStatus() == MatchStatus.FINISHED)
                                .count();

                        totalScratchedMatches += leagueMatches.stream()
                                .filter(match -> match.getStatus() == MatchStatus.SCRATCHED)
                                .count();

                        totalCancelledMatches += leagueMatches.stream()
                                .filter(match -> match.getStatus() == MatchStatus.CANCELLED)
                                .count();

                        totalCompletedMatches += leagueMatches.stream()
                                .filter(match -> match.getStatus().isCompleted())
                                .count();
                    }

                    return seasonMapper.mapToTennisSeasonListDTO(
                            season,
                            seasonLeagues.size(),
                            totalPlayers,
                            totalTeams,
                            participantIds.size(),
                            totalMatches,
                            totalFinishedMatches,
                            totalScratchedMatches,
                            totalCancelledMatches,
                            totalCompletedMatches
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public List<VolleySeasonListResponseDTO> getVolleySeasons(
            List<SeasonStatus> statuses) {

        List<Season> seasons;

        if (statuses == null || statuses.isEmpty()) {
            seasons = seasonRepository.findAllByOrderByYearDesc();
        } else {
            seasons = seasonRepository.findByStatusInOrderByYearDesc(statuses);
        }

        if (seasons.isEmpty()) {
            return List.of();
        }

        Set<String> seasonIds = seasons.stream()
                .map(Season::getId)
                .collect(Collectors.toSet());

        List<VolleyLeague> volleyLeagues =
                volleyLeagueRepository.findBySeasonIdInWithTeams(seasonIds);

        Map<String, List<VolleyLeague>> leaguesBySeason =
                volleyLeagues.stream()
                        .collect(Collectors.groupingBy(
                                league -> league.getSeason().getId()
                        ));

        // Nechceme sezóny bez volleyball ligy
        seasons = seasons.stream()
                .filter(season -> leaguesBySeason.containsKey(season.getId()))
                .toList();

        if (seasons.isEmpty()) {
            return List.of();
        }

        Set<String> leagueIds = volleyLeagues.stream()
                .map(VolleyLeague::getId)
                .collect(Collectors.toSet());

        List<Match> matches =
                leagueIds.isEmpty()
                        ? List.of()
                        : matchRepository.findByLeagueIdIn(leagueIds);

        Map<String, List<Match>> matchesByLeague =
                matches.stream()
                        .collect(Collectors.groupingBy(
                                Match::getLeagueId
                        ));

        return seasons.stream()
                .map(season -> {

                    List<VolleyLeague> seasonLeagues =
                            leaguesBySeason.get(season.getId());

                    long totalTeams = 0;
                    long totalMatches = 0;
                    long totalFinishedMatches = 0;
                    long totalScratchedMatches = 0;
                    long totalCancelledMatches = 0;
                    long totalCompletedMatches = 0;

                    for (VolleyLeague league : seasonLeagues) {

                        if (league.getTeams() != null) {
                            totalTeams += league.getTeams().size();
                        }

                        List<Match> leagueMatches =
                                matchesByLeague.getOrDefault(
                                        league.getId(),
                                        List.of()
                                );

                        totalMatches += leagueMatches.size();

                        totalFinishedMatches += leagueMatches.stream()
                                .filter(match -> match.getStatus() == MatchStatus.FINISHED)
                                .count();

                        totalScratchedMatches += leagueMatches.stream()
                                .filter(match -> match.getStatus() == MatchStatus.SCRATCHED)
                                .count();

                        totalCancelledMatches += leagueMatches.stream()
                                .filter(match -> match.getStatus() == MatchStatus.CANCELLED)
                                .count();

                        totalCompletedMatches += leagueMatches.stream()
                                .filter(match -> match.getStatus().isCompleted())
                                .count();
                    }

                    return seasonMapper.mapToVolleySeasonListDTO(
                            season,
                            seasonLeagues.size(),
                            totalTeams,
                            totalMatches,
                            totalFinishedMatches,
                            totalScratchedMatches,
                            totalCancelledMatches,
                            totalCompletedMatches
                    );
                })
                .toList();
    }


    @Override
    public SeasonResponseDTO createSeason(CreateSeasonDTO createSeasonDTO) {
        Season season = Season.builder()
                .id(UUID.randomUUID().toString())
                .year(createSeasonDTO.getYear())
                .build();
        season = seasonRepository.save(season);
        return seasonMapper.mapSeasonToDTO(season, false);
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

        seasonRepository.findByStatus(SeasonStatus.ACTIVE)
                .filter(active -> !active.getId().equals(seasonId))
                .ifPresent(active -> {
                    throw new IllegalStateException(
                            "Nie je možné spustiť sezónu – sezóna " +
                                    active.getYear() + " je už aktívna."
                    );
                });

        if (season.getStatus() != SeasonStatus.CREATED) {
            throw switch (season.getStatus()) {
                case ACTIVE -> new IllegalStateException(
                        "Sezóna " + season.getYear() + " je už spustená."
                );
                case FINISHED -> new IllegalStateException(
                        "Sezóna " + season.getYear() + " bola už ukončená."
                );
                default -> new IllegalStateException(
                        "Sezónu možno spustiť len ak je v stave CREATED."
                );
            };
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

        log.info("Season {} has been successfully started.", season.getYear());

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

        log.info("Season {} has successfully finished.", season.getYear());

        return "Sezóna " + season.getYear() + " bola ukončená spolu s jej ligami.";
    }

    private Season getSeasonOrThrow(String id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No season found with id: " + id));
    }
}
