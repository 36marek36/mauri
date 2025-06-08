package com.example.mauri.service;

import com.example.mauri.model.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();
    User getAuthenticatedUser();
    void assignPlayerToUser(String playerId, String userId);
}
