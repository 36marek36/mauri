package com.example.mauri.controller;

import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.CreatePlayerDTO;
import com.example.mauri.service.PlayerService;
import com.example.mauri.service.UserService;
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

    @Autowired
    public PlayerApi(PlayerService playerService, UserService userService) {
        this.playerService = playerService;
        this.userService = userService;
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

    @PostMapping("/admin/create")
    ResponseEntity<Player> createPlayer(@RequestBody CreatePlayerDTO createPlayerDTO) {
        Player created = playerService.createPlayer(createPlayerDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/user/create")
    public ResponseEntity<Player> createAndAssignPlayerForCurrentUser(@RequestBody CreatePlayerDTO createPlayerDTO) {
        User user = userService.getAuthenticatedUser();

        if (user.getPlayer() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Už má hráča
        }

        Player player = playerService.createPlayer(createPlayerDTO);
        userService.assignPlayerToUser(player.getId(), user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(player);
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
