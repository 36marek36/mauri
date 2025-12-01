package com.example.mauri.controller;

import com.example.mauri.model.dto.response.UserResponseDTO;
import com.example.mauri.model.dto.update.ShowDetailsUpdateDTO;
import com.example.mauri.model.dto.update.UpdateRoleDTO;
import com.example.mauri.model.dto.update.UpdateUsernameDTO;
import com.example.mauri.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getAuthenticatedUser());
    }

    @PatchMapping("/me/update")
    ResponseEntity<String> updateUsername(@RequestBody UpdateUsernameDTO updateUsernameDTO) {
        String message = userService.updateUsernameForAuthenticatedUser(updateUsernameDTO.getNewUsername());
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/updateRole")
    ResponseEntity<String> updateUserRole(@RequestBody @Valid UpdateRoleDTO updateRoleDTO) {
        String message = userService.updateUserRole(updateRoleDTO);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/showPlayerDetails")
    ResponseEntity<String> showUserDetails(@RequestBody ShowDetailsUpdateDTO showDetailsUpdateDTO) {
        String message = userService.showDetails(showDetailsUpdateDTO);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
