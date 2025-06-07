package com.example.mauri.controller;

import com.example.mauri.model.User;
import com.example.mauri.service.UserService;
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
}
