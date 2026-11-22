package com.example.mauri.enums;

public enum MatchStatus {
    CREATED,FINISHED,CANCELLED,SCRATCHED,PENDING_APPROVAL,;

    public boolean isPlayed() {
        return this == FINISHED
                || this == SCRATCHED;
    }

    public boolean isCompleted() {
        return isPlayed()
                || this == CANCELLED;
    }
}