package com.example.mauri.service.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.UserRepository;
import com.example.mauri.service.UserService;
import jakarta.transaction.Transactional;
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
    private final PlayerRepository playerRepository;


    @Override
    public List<User> getUsers() {
        return userRepository.findAll().stream().toList();
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("username {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    @Override
    public void assignPlayerToUser(String playerId, String userId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getPlayer()!=null) {
            throw new IllegalStateException("User already has a player assigned");
        }

        user.setPlayer(player);
        userRepository.save(user);
    }
}
