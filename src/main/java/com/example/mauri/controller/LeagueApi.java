package com.example.mauri.controller;

import com.example.mauri.model.League;
import com.example.mauri.model.Match;
import com.example.mauri.model.dto.AddParticipantsToLeagueDTO;
import com.example.mauri.model.dto.CreateLeagueDTO;
import com.example.mauri.model.dto.PlayerStatsDTO;
import com.example.mauri.model.dto.TeamStatsDTO;
import com.example.mauri.service.LeagueService;
import com.example.mauri.service.MatchService;
import com.example.mauri.service.PlayerStatsService;
import com.example.mauri.service.TeamStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/leagues")
@Slf4j
public class LeagueApi {

    private final LeagueService leagueService;
    private final MatchService matchService;
    private final TeamStatsService teamStatsService;
    private final PlayerStatsService playerStatsService;

    @Autowired
    public LeagueApi(LeagueService leagueService, MatchService matchService, TeamStatsService teamStatsService, PlayerStatsService playerStatsService) {
        this.leagueService = leagueService;
        this.matchService = matchService;
        this.teamStatsService = teamStatsService;
        this.playerStatsService = playerStatsService;
    }

    @GetMapping("/")
    public List<League> getLeagues() {
        return leagueService.getAllLeagues();
    }

    @GetMapping("/{id}")
    public League getLeagueById(@PathVariable("id") String id) {
        return leagueService.getLeagueById(id);
    }

    @GetMapping("/{leagueId}/matches")
    public ResponseEntity<List<Match>> getMatchesByLeagueId(@PathVariable("leagueId") String leagueId) {
        List<Match> matches = matchService.getMatchesForLeague(leagueId);
        return new ResponseEntity<>(matches, HttpStatus.OK);
    }

    @GetMapping("/no-season")
    public ResponseEntity<List<League>> getNoSeasonLeagues() {
        List<League> leagues = leagueService.getLeaguesWithoutSeason();
        return new ResponseEntity<>(leagues, HttpStatus.OK);
    }

    @GetMapping("/{leagueId}/teams/stats")
    public List<TeamStatsDTO> getTeamStatsForLeague(@PathVariable String leagueId) {
        return teamStatsService.getAllStatsForLeague(leagueId);
    }

    @GetMapping("/{leagueId}/teams/{teamId}/stats")
    public ResponseEntity<TeamStatsDTO> getTeamStats(@PathVariable String leagueId, @PathVariable String teamId) {
        return ResponseEntity.ok(teamStatsService.getTeamStats(leagueId, teamId));
    }

    @GetMapping("/{leagueId}/players/stats")
    public List<PlayerStatsDTO> getPlayerStatsForLeague(@PathVariable String leagueId) {
        return playerStatsService.getAllStatsForLeague(leagueId);
    }

    @GetMapping("/{leagueId}/players/{playerId}/stats")
    public ResponseEntity<PlayerStatsDTO> getPlayerStats(@PathVariable String leagueId, @PathVariable String playerId) {
        return ResponseEntity.ok(playerStatsService.getPlayerStats(leagueId, playerId));
    }

    @GetMapping("/{leagueId}/progress")
    public ResponseEntity<Integer> getLeagueProgress(@PathVariable String leagueId) {
        int progress = leagueService.progress(leagueId);
        return ResponseEntity.ok(progress);
    }


    @PostMapping("/create")
    ResponseEntity<League> createLeague(@RequestBody CreateLeagueDTO createLeagueDTO) {
        League createdLeague = leagueService.createLeague(createLeagueDTO);
        return new ResponseEntity<>(createdLeague, HttpStatus.CREATED);
    }

    @PatchMapping("/{leagueId}/addParticipants")
    public ResponseEntity<League> addParticipantsToLeague(
            @PathVariable String leagueId,
            @RequestBody AddParticipantsToLeagueDTO dto) {

        League updatedLeague = leagueService.addParticipantsToLeague(leagueId, dto.getParticipantIds());
        return new ResponseEntity<>(updatedLeague, HttpStatus.OK);
    }

    @PatchMapping("/{leagueId}/finish")
    public ResponseEntity<String> finishLeague(@PathVariable String leagueId) {
        log.info("league finished");
        try {
            leagueService.finishLeague(leagueId);
            return ResponseEntity.ok("league finished");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    ResponseEntity<League> deleteLeague(@PathVariable("id") String id) {
        leagueService.deleteLeagueById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{leagueId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipantFromLeague(
            @PathVariable String leagueId,
            @PathVariable String participantId) {

        leagueService.removeParticipantFromLeague(leagueId, participantId);
        return ResponseEntity.noContent().build();
    }


}
