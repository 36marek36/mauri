package com.example.mauri.controller;

import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.AssignPlayerDTO;
import com.example.mauri.model.dto.CreatePlayerDTO;
import com.example.mauri.model.dto.LeagueDTO;
import com.example.mauri.service.LeagueService;
import com.example.mauri.service.PlayerService;
import com.example.mauri.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/players")
@Slf4j
public class PlayerApi {
    private final PlayerService playerService;
    private final UserService userService;
    private final LeagueService leagueService;

    @Autowired
    public PlayerApi(PlayerService playerService, UserService userService, LeagueService leagueService) {
        this.playerService = playerService;
        this.userService = userService;
        this.leagueService = leagueService;
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
    @GetMapping("/{playerId}/leagues")
    public ResponseEntity<List<LeagueDTO>> getLeaguesForPlayer(@PathVariable String playerId) {
        List<LeagueDTO> leagues = leagueService.getLeaguesForPlayer(playerId);
        return ResponseEntity.ok(leagues);
    }

    @PostMapping("/admin/createPlayer")
    ResponseEntity<Player> createPlayer(@Valid @RequestBody CreatePlayerDTO createPlayerDTO) {
        Player created = playerService.createPlayer(createPlayerDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/user/createPlayer")
    public ResponseEntity<Player> createAndAssignPlayerForCurrentUser(@RequestBody CreatePlayerDTO createPlayerDTO) {
        User user = userService.getAuthenticatedUser();

        if (user.getPlayer() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Player player = playerService.createPlayer(createPlayerDTO);
        userService.assignPlayerToUser(player.getId(), user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(player);
    }

    @PatchMapping("/assignToUser/{userId}")
    public ResponseEntity<String> assignPlayerToUser(@PathVariable String userId,
                                                     @RequestBody AssignPlayerDTO assignPlayer) {
        userService.assignPlayerToUser(assignPlayer.getPlayerId(), userId);
        return ResponseEntity.status(HttpStatus.OK).body("Player assigned to user");
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Player> deletePlayer(@PathVariable String id) {
        playerService.deletePlayer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
