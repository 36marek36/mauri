package com.example.mauri.model.dto;

import com.example.mauri.model.Player;
import com.example.mauri.model.User;
import lombok.Data;


@Data
public class UserDTO {
    private String username;
    private String role;
    private String playerId;
    private String playerName;

    public UserDTO(User user) {
        this.username = user.getUsername();
        this.role = user.getRole().name();
        Player player = user.getPlayer();
        if (player != null) {
            this.playerId = player.getId();
            this.playerName = player.getFirstName() + " " + player.getLastName();
        } else {
            this.playerId = null;
            this.playerName = null;
        }
    }
}
