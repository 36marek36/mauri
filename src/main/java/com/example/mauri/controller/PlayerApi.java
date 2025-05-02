package com.example.mauri.controller;

import com.example.mauri.model.Player;
import com.example.mauri.service.PlayerService;
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

    @Autowired
    public PlayerApi(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/")
    List<Player> getPlayers() {
        return playerService.getPlayers();
    }

    @GetMapping("/{id}")
    Player getPlayer(@PathVariable String id) {
        return playerService.getPlayer(id);
    }

    @PostMapping("/create")
    ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        Player created = playerService.addPlayer(player.getFirstName(), player.getLastName(), player.getEmail(), player.getPhone(), player.getRegistrationDate());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
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
