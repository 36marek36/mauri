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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/players")
@RequiredArgsConstructor
@Slf4j
public class PlayerController {
    private final PlayerService playerService;
    private final UserService userService;
    private final LeagueService leagueService;

    @GetMapping("/")
    List<Player> getActivePlayers() {
        return playerService.getActivePlayers();
    }

    @GetMapping("/inactive")
    List<Player> getInactivePlayers() {
        return playerService.getInactivePlayers();
    }

    @GetMapping("/{id}")
    Player getPlayer(@PathVariable String id) {
        return playerService.getPlayer(id);
    }

    @GetMapping("/not-in-any-active-league")
    public ResponseEntity<List<Player>> getFreePlayers() {
        List<Player> freePlayers = playerService.getActivePlayersNotInAnyActiveLeague();
        return new ResponseEntity<>(freePlayers, HttpStatus.OK);
    }
    @GetMapping("/{playerId}/leagues")
    public ResponseEntity<List<LeagueDTO>> getLeaguesForPlayer(@PathVariable String playerId) {
        List<LeagueDTO> leagues = leagueService.getLeaguesForPlayer(playerId);
        return ResponseEntity.ok(leagues);
    }

    @GetMapping("/without-user")
    public ResponseEntity<List<Player>> getPlayersWithoutUser() {
        List<Player> players = playerService.getPlayersWithoutUser();
        return ResponseEntity.ok(players);
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
    ResponseEntity<Void> deletePlayer(@PathVariable String id) {
        playerService.deletePlayer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
