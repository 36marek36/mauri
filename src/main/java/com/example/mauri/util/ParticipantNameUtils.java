package com.example.mauri.util;

import com.example.mauri.model.Player;
import com.example.mauri.model.Team;

public class ParticipantNameUtils {
    public static String buildPlayerName(Player player) {
        String first = player.getFirstName() != null ? player.getFirstName() : "";
        String last = player.getLastName() != null ? player.getLastName() : "";
        return (last + " " + first).trim();
    }
    public static String buildPlayerShortName(Player player) {
        String first = player.getFirstName() != null ? player.getFirstName() : "";
        String last = player.getLastName() != null ? player.getLastName() : "";

        String firstInitial = !first.isEmpty()
                ? first.substring(0, 1).toUpperCase() + "."
                : "";

        return (last + " " + firstInitial).trim();
    }

    public static String buildTeamName(Team team) {
        String name1 = team.getPlayer1() != null ? buildPlayerName(team.getPlayer1()) : "";
        String name2 = team.getPlayer2() != null ? buildPlayerName(team.getPlayer2()) : "";
        return (name1 + " a " + name2).trim();
    }
    public static String buildTeamShortName(Team team) {
        String p1 = team.getPlayer1() != null ? buildPlayerShortName(team.getPlayer1()) : "";
        String p2 = team.getPlayer2() != null ? buildPlayerShortName(team.getPlayer2()) : "";

        return (p1 + " & " + p2).trim();
    }

    public static String capitalizeNamePart(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        input = input.trim();
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

}
