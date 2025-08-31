package com.example.mauri.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class UserResponseDTO {
    private String id;
    private String username;
    private String role;
    private String playerId;
    private String playerName;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

}
