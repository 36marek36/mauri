package com.example.mauri.enums;

public enum PlayerLevel {
    BEGINNER,
    AMATEUR,
    INTERMEDIATE,
    ADVANCED,
    PROFESSIONAL,
    MASTER;

    public static PlayerLevel fromRating(int rating) {
        if (rating < 900) return BEGINNER;
        if (rating < 1100) return AMATEUR;
        if (rating < 1300) return INTERMEDIATE;
        if (rating < 1500) return ADVANCED;
        if (rating < 1800) return PROFESSIONAL;
        return MASTER;
    }
}
