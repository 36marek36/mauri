package com.example.mauri.enums;

public enum DoublePlayerLevel {
    BEGINNER,
    AMATEUR,
    INTERMEDIATE,
    ADVANCED,
    PROFESSIONAL,
    ELITE,
    LEGEND;

    public static DoublePlayerLevel fromRating(int rating) {
        if (rating < 900) return BEGINNER;
        if (rating < 1100) return AMATEUR;
        if (rating < 1300) return INTERMEDIATE;
        if (rating < 1500) return ADVANCED;
        if (rating < 1700) return PROFESSIONAL;
        if (rating < 1900) return ELITE;
        return LEGEND;
    }
}
