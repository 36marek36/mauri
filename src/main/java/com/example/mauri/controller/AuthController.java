package com.example.mauri.controller;

import com.example.mauri.model.dto.ChangePasswordDTO;
import com.example.mauri.security.dto.LoginRequest;
import com.example.mauri.security.dto.LoginResponse;
import com.example.mauri.security.dto.RegisterRequest;
import com.example.mauri.security.dto.RegisterResponse;
import com.example.mauri.service.AuthServiceBean;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceBean authServiceBean;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authServiceBean.authenticate(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authServiceBean.register(request));
    }

    @PatchMapping ("/change-password")
    public ResponseEntity<String> changePassword (@RequestBody ChangePasswordDTO request) {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        authServiceBean.changePassword(currentUserName, request);
        return ResponseEntity.ok("Heslo bolo úspešne zmenené");
    }

}
