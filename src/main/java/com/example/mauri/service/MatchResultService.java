package com.example.mauri.service;

import com.example.mauri.model.Match;
import com.example.mauri.model.MatchResult;

public interface MatchResultService {

    MatchResult processResult(Match match, MatchResult inputResult);
}
