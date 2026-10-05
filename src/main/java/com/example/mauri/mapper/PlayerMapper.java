package com.example.mauri.mapper;

import com.example.mauri.model.Player;
import com.example.mauri.model.dto.request.PlayerShortDTO;
import com.example.mauri.model.dto.response.PlayerResponseDTO;

import com.example.mauri.util.ParticipantNameUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PlayerMapper {

    public PlayerResponseDTO mapToResponseDTO(Player player) {
        return PlayerResponseDTO.builder()
                .id(player.getId())
                .firstName(player.getFirstName())
                .lastName(player.getLastName())
                .name(player.getLastName() + " " + player.getFirstName())
                .email(player.getEmail())
                .phone(player.getPhone())
                .registrationDate(player.getRegistrationDate())
                .deletedDate(player.getDeletedDate())
                .active(player.isActive())
                .sports(new ArrayList<>(player.getSports()))
                .build();
    }

    public PlayerShortDTO mapToPlayerShortDTO(Player player) {
        return PlayerShortDTO.builder()
                .id(player.getId())
                .name(ParticipantNameUtils.buildPlayerName(player))
                .build();
    }
}
