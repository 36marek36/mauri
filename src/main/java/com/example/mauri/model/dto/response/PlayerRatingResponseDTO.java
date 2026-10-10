package com.example.mauri.model.dto.response;

import com.example.mauri.enums.DoublePlayerLevel;
import com.example.mauri.enums.PlayerLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerRatingResponseDTO {
    private String playerId;
    private String playerName;
    private Integer rating;
    private PlayerLevel playerLevel;
    private int ratingChange;
    private int doubleRating;
    private DoublePlayerLevel doublePlayerLevel;
    private int doubleRatingChange;

}
