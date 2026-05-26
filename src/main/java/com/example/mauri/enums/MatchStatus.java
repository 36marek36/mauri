package com.example.mauri.enums;

public enum MatchStatus {
    CREATED,FINISHED,CANCELLED,SCRATCHED;

    public boolean isPlayed() {
        return this == FINISHED
                || this == CANCELLED
                || this == SCRATCHED;
    }
}
