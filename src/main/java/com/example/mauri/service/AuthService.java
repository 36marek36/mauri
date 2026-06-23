package com.example.mauri.service;

import com.example.mauri.model.dto.request.ChangePasswordDTO;
import com.example.mauri.security.dto.LoginRequest;
import com.example.mauri.security.dto.LoginResponse;
import com.example.mauri.security.dto.RegisterRequest;
import com.example.mauri.security.dto.RegisterResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    LoginResponse authenticate(LoginRequest request);
    RegisterResponse register(RegisterRequest request);
    void changePassword(String currentUsername, ChangePasswordDTO request);
    void logout(Authentication authentication);
}
