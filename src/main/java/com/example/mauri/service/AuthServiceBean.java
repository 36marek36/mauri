package com.example.mauri.service;

import com.example.mauri.enums.Role;
import com.example.mauri.model.User;
import com.example.mauri.repository.UserRepository;
import com.example.mauri.security.JwtUtil;
import com.example.mauri.security.dto.LoginRequest;
import com.example.mauri.security.dto.LoginResponse;
import com.example.mauri.security.dto.RegisterRequest;
import com.example.mauri.security.dto.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceBean {

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
            throw new RuntimeException("Invalid username/password");
        }

        var userDetails = userDetailsService.loadUserByUsername(request.username());
        var token = jwtUtil.generateToken(userDetails.getUsername());
        return new LoginResponse(token);
    }

    public RegisterResponse register(RegisterRequest request) {
        // 1. Skontroluj, či užívateľ neexistuje
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Používateľ s týmto menom už existuje");
        }

        // 2. Vytvor používateľa
        var user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        userRepository.save(user); // ulož do DB

        // 3. Vráť len správu, že používateľ bol vytvorený
        return new RegisterResponse("Užívateľ bol úspešne vytvorený");
    }
}
