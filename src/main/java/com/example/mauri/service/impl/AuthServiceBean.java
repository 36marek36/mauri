package com.example.mauri.service.impl;

import com.example.mauri.enums.Role;
import com.example.mauri.exception.InvalidCredentialsException;
import com.example.mauri.exception.InvalidOldPasswordException;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.exception.UsernameAlreadyExistsException;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.request.ChangePasswordDTO;
import com.example.mauri.repository.UserRepository;
import com.example.mauri.security.JwtUtil;
import com.example.mauri.security.dto.LoginRequest;
import com.example.mauri.security.dto.LoginResponse;
import com.example.mauri.security.dto.RegisterRequest;
import com.example.mauri.security.dto.RegisterResponse;
import com.example.mauri.service.AuthService;
import com.example.mauri.util.ParticipantNameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceBean implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse authenticate(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for user '{}'", request.username());
            throw new InvalidCredentialsException("Neplatné prihlasovacie údaje");
        }

        var userDetails = userDetailsService.loadUserByUsername(request.username());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        var token = jwtUtil.generateToken(userDetails.getUsername());

        if (user.getPlayer() != null) {
            log.info("'{}' logged in successfully - {}",
                    user.getUsername(),
                    ParticipantNameUtils.buildPlayerName(user.getPlayer()));
        } else {
            log.info("'{}' logged in successfully - no player profile", user.getUsername());
        }

        return new LoginResponse(token);
    }

    public RegisterResponse register(RegisterRequest request) {
        // 1. Skontroluj, či užívateľ neexistuje
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Používateľ s týmto menom už existuje");
        }

        // 2. Vytvor používateľa
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user); // ulož do DB

        log.info("New user '{}' has registered successfully", user.getUsername());

        // 3. Vráť len správu, že používateľ bol vytvorený
        return new RegisterResponse("Užívateľ bol úspešne vytvorený");
    }

    public void changePassword(String currentUsername, ChangePasswordDTO request) {
        var user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("'{}' failed to change password", currentUsername);
            throw new InvalidOldPasswordException("Nesprávne staré heslo!");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("'{}' changed password successfully", currentUsername);
    }

    @Override
    public void logout(Authentication authentication) {
        String username = authentication.getName();
        log.info("'{}' logged out", username);
    }
}
