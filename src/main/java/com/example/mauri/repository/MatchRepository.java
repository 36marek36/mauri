package com.example.mauri.repository;

import com.example.mauri.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, String> {
    boolean existsByLeagueId(String leagueId);
}
