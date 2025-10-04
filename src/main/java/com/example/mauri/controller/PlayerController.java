package com.example.mauri.controller;

import com.example.mauri.model.dto.request.AssignPlayerDTO;
import com.example.mauri.model.dto.create.CreatePlayerDTO;
import com.example.mauri.model.dto.response.PlayerResponseDTO;
import com.example.mauri.model.dto.response.UserResponseDTO;
import com.example.mauri.model.dto.update.UpdatePlayerDTO;
import com.example.mauri.service.PlayerService;
import com.example.mauri.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = "https://preeminent-boba-fd56c6.netlify.app")
@RestController
@RequestMapping("/rest/players")
@RequiredArgsConstructor
@Slf4j
public class PlayerController {
    private final PlayerService playerService;
    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<List<PlayerResponseDTO>> getActivePlayers() {
        List<PlayerResponseDTO> players = playerService.getActivePlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<PlayerResponseDTO>> getInactivePlayers() {
        List<PlayerResponseDTO> players = playerService.getInactivePlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> getPlayer(@PathVariable String id) {
        PlayerResponseDTO player = playerService.getPlayerResponseById(id);
        return ResponseEntity.ok(player);
    }

    @GetMapping("/not-in-any-active-league")
    public ResponseEntity<List<PlayerResponseDTO>> getFreePlayers() {
        List<PlayerResponseDTO> freePlayersDTO = playerService.getActivePlayersNotInAnyActiveLeague();
        return ResponseEntity.ok(freePlayersDTO);
    }

    @GetMapping("/without-user")
    public ResponseEntity<List<PlayerResponseDTO>> getPlayersWithoutUser() {
        List<PlayerResponseDTO> players = playerService.getPlayersWithoutUser();
        return ResponseEntity.ok(players);
    }

    @PostMapping("/admin/createPlayer")
    ResponseEntity<PlayerResponseDTO> createPlayer(@Valid @RequestBody CreatePlayerDTO createPlayerDTO) {
        PlayerResponseDTO created = playerService.createPlayer(createPlayerDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/user/createPlayer")
    public ResponseEntity<PlayerResponseDTO> createAndAssignPlayerForCurrentUser(@Valid @RequestBody CreatePlayerDTO createPlayerDTO) {
        UserResponseDTO user = userService.getAuthenticatedUser();
        PlayerResponseDTO player = playerService.createAndAssignPlayerToUser(createPlayerDTO, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(player);
    }

    @PatchMapping("/assignToUser/{userId}")
    public ResponseEntity<String> assignPlayerToUser(@PathVariable String userId,
                                                     @RequestBody AssignPlayerDTO assignPlayer) {
        playerService.assignPlayerToUser(assignPlayer.getPlayerId(), userId);
        return ResponseEntity.ok("Hráč bol úspešne priradený.");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> updatePlayer(@PathVariable String id,
                                                          @Valid @RequestBody UpdatePlayerDTO updatedPlayer) {
        PlayerResponseDTO updated = playerService.updatePlayer(id, updatedPlayer);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePlayer(@PathVariable String id) {
        String status = playerService.deletePlayer(id);

        Map<String, String> response = Map.of("status", status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
