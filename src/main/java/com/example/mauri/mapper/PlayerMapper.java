package com.example.mauri.mapper;

import com.example.mauri.model.Player;
import com.example.mauri.model.dto.response.PlayerResponseDTO;

import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public PlayerResponseDTO mapToResponseDTO(Player player) {
        return PlayerResponseDTO.builder()
                .id(player.getId())
                .firstName(player.getFirstName())
                .lastName(player.getLastName())
                .name(player.getFirstName() + " " + player.getLastName())
                .email(player.getEmail())
                .phone(player.getPhone())
                .registrationDate(player.getRegistrationDate())
                .deletedDate(player.getDeletedDate())
                .active(player.isActive())
                .build();
    }
}
