package com.example.mauri.service.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.UserMapper;
import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.response.UserResponseDTO;
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

}
