package com.example.mauri.controller;

import com.example.mauri.controller.request.CreateUser;
import com.example.mauri.model.User;
import com.example.mauri.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/users")
public class UserApi {

    private final UserService userService;

    public UserApi(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    List<User> getAllUsers() {
        return userService.getUsers();
    }

    @PostMapping("/create")
    ResponseEntity<String> createUser(@RequestBody CreateUser user) {
        userService.addUser(user.username(), user.password());
        return new ResponseEntity<>("User created", HttpStatus.CREATED);
    }
}
