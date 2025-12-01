package com.example.mauri.service;

import com.example.mauri.model.dto.response.UserResponseDTO;
import com.example.mauri.model.dto.update.ShowDetailsUpdateDTO;
import com.example.mauri.model.dto.update.UpdateRoleDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getUsers();
    UserResponseDTO getAuthenticatedUser();
    void deleteUser(String userId);
    String updateUsernameForAuthenticatedUser(String newUsername);
    String updateUserRole(UpdateRoleDTO dto);
    String showDetails(ShowDetailsUpdateDTO showDetailsUpdateDTO);
}
