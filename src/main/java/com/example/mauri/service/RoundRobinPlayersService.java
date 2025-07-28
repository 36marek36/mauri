package com.example.mauri.service;

import com.example.mauri.enums.MatchType;
import com.example.mauri.model.Match;
import com.example.mauri.model.Player;

import java.util.List;

public interface RoundRobinPlayersService {
    List<Match> generateMatches(List<Player> players, String leagueId, MatchType matchType);
}
