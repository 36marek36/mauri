package com.example.mauri.model.dto.update;

import lombok.Data;

@Data
public class ChangeTeamDTO {
    private String oldPlayerId;
    private String newPlayerId;
}
