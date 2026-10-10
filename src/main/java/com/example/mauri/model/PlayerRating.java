package com.example.mauri.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = "player_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerRating {
    @Id
    private String id;
    @OneToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "player_id",nullable = false)
    private Player player;

    @Builder.Default
    @Column(nullable = false)
    private int rating = 1000;

    @Column(nullable = false)
    private int ratingChange;

    @Column(nullable = false)
    private int doubleRating;

    @Column(nullable = false)
    private int doubleRatingChange;
}
