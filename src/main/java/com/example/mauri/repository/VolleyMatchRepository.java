package com.example.mauri.repository;

import com.example.mauri.model.VolleyMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolleyMatchRepository extends JpaRepository<VolleyMatch, String> {
    List<VolleyMatch> findByVolleyLeagueId(String leagueId);

    @Query("SELECT m FROM volleyball_matches m WHERE m.volleyLeagueId = :leagueId AND (m.homeTeam.id = :teamId or m.awayTeam.id = :teamId)")
    List<VolleyMatch> findMatchesByLeagueAndTeam(@Param("leagueId") String leagueId, @Param("teamId") String teamId);
}
