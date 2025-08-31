package com.example.mauri.mapper;

import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import com.example.mauri.model.dto.response.UserResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO mapUserToDTO (User user){

        Player player = user.getPlayer();

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .playerId(player != null ? player.getId() : null)
                .playerName(player != null ? player.getFirstName() + " " + player.getLastName() : null)
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
