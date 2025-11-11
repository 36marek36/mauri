package com.example.mauri.service.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.UserMapper;
import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.response.UserResponseDTO;
import com.example.mauri.model.dto.update.UpdateRoleDTO;
import com.example.mauri.repository.UserRepository;
import com.example.mauri.service.PlayerService;
import com.example.mauri.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceBean implements UserService {
    private final UserRepository userRepository;
    private final PlayerService playerService;
    private final UserMapper userMapper;


    @Override
    public List<UserResponseDTO> getUsers(){
        return userRepository.findAll().stream()
                .map(userMapper::mapUserToDTO)
                .toList();
    }

    @Override
    public UserResponseDTO getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("username {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.mapUserToDTO(user);
    }

    @Override
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Player player = user.getPlayer();
        if (player!=null) {
            // najprv odpojiť hráča od používateľa
            user.setPlayer(null);
            userRepository.save(user);

            // zavolať bezpečné zmazanie/deaktiváciu hráča
            playerService.deletePlayer(player.getId());
        }
        userRepository.deleteById(userId);
    }

    @Override
    public String updateUsernameForAuthenticatedUser(String newUsername) {
        // Získaj username prihláseného používateľa zo Spring Security kontextu
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Nájde používateľa podľa username
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Voliteľná kontrola, či username už existuje (a nie je rovnaký)
        if (userRepository.findByUsername(newUsername).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        // Nastav nové meno a ulož
        user.setUsername(newUsername);
        userRepository.save(user);
        return "Užívatelske meno bolo úspešne zmenené";
    }

    @Override
    public String updateUserRole(UpdateRoleDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Používateľ nebol nájdený"));

        user.setRole(dto.getUpdateUserRole());
        userRepository.save(user);

        return "Rola používateľa " + user.getUsername() + " bola zmenená na " + dto.getUpdateUserRole();
    }

}
