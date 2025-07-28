package com.example.mauri.controller;

import com.example.mauri.model.User;
import com.example.mauri.model.dto.UserDTO;
import com.example.mauri.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/")
    List<User> getAllUsers() {
        return userService.getUsers();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(new UserDTO(user));
    }
}
