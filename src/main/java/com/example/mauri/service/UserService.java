package com.example.mauri.service;

import com.example.mauri.model.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getUsers();
    UserResponseDTO getAuthenticatedUser();
    void deleteUser(String userId);
}
