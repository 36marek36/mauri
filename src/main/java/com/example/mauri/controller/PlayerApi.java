package com.example.mauri.controller;

import com.example.mauri.model.Player;
import com.example.mauri.model.dto.PlayerStatsDTO;
import com.example.mauri.service.PlayerService;
import com.example.mauri.service.PlayerStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rest/players")
@Slf4j
public class PlayerApi {
    private final PlayerService playerService;
    private final PlayerStatsService playerStatsService;

    @Autowired
    public PlayerApi(PlayerService playerService, PlayerStatsService playerStatsService) {
        this.playerService = playerService;
        this.playerStatsService = playerStatsService;
    }

    @GetMapping("/")
    List<Player> getPlayers() {
        return playerService.getPlayers();
    }

    @GetMapping("/{id}")
    Player getPlayer(@PathVariable String id) {
        return playerService.getPlayer(id);
    }

    @GetMapping("/not-in-any-league")
    public ResponseEntity<List<Player>> getFreePlayers() {
        List<Player> freePlayers = playerService.getPlayersNotInAnyLeague();
        return new ResponseEntity<>(freePlayers, HttpStatus.OK);
    }

    @GetMapping("/{playerId}/leagues/{leagueId}/stats")
    public ResponseEntity<PlayerStatsDTO> getPlayerStats(@PathVariable String leagueId, @PathVariable String playerId) {
        return ResponseEntity.ok(playerStatsService.getPlayerStats(leagueId, playerId));
    }

    @PostMapping("/create")
    ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        Player created = playerService.addPlayer(player.getFirstName(), player.getLastName(), player.getEmail(), player.getPhone(), LocalDate.now());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Player> deletePlayer(@PathVariable String id) {
        playerService.deletePlayer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Player> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("handleIllegalArgumentException: {}", e.getMessage());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
