package com.example.mauri.service;

import com.example.mauri.enums.MatchType;
import com.example.mauri.model.Match;
import com.example.mauri.model.Team;

import java.util.List;

public interface RoundRobinTeamsService {
    List<Match> generateMatches(List<Team> teams, String leagueId, MatchType matchType);
}
