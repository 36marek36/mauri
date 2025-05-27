package com.example.mauri.repository;

import com.example.mauri.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, String> {
    boolean existsByLeagueId(String leagueId);
    List<Match> findByLeagueId(String leagueId);
//    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
//            "FROM matches m " +
//            "WHERE m.leagueId = :leagueId " +
//            "AND ((m.homePlayer.id = :p1 AND m.awayPlayer.id = :p2) " +
//            "OR (m.homePlayer.id = :p2 AND m.awayPlayer.id = :p1))")
//    boolean existsByLeagueIdAndPlayers(@Param("leagueId") String leagueId,
//                                       @Param("p1") String p1,
//                                       @Param("p2") String p2);
//
//    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
//            "FROM matches m " +
//            "WHERE m.leagueId = :leagueId " +
//            "AND ((m.homeTeam.id = :t1 AND m.awayTeam.id = :t2) " +
//            "OR (m.homeTeam.id = :t2 AND m.awayTeam.id = :t1))")
//    boolean existsByLeagueIdAndTeams(@Param("leagueId") String leagueId,
//                                     @Param("t1") String t1,
//                                     @Param("t2") String t2);

    @Query("SELECT m FROM matches m WHERE m.leagueId = :leagueId AND " +
            "(m.homePlayer.id = :playerId OR m.awayPlayer.id = :playerId)")
    List<Match> findByLeagueIdAndPlayer(@Param("leagueId") String leagueId,
                                        @Param("playerId") String playerId);

    @Query("SELECT m FROM matches m WHERE m.leagueId = :leagueId AND " +
            "(m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId)")
    List<Match> findByLeagueIdAndTeam(@Param("leagueId") String leagueId,
                                      @Param("teamId") String teamId);
}
