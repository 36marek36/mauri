package com.example.mauri.repository;

import com.example.mauri.enums.MatchStatus;
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

    @Query("SELECT m FROM matches m WHERE m.leagueId = :leagueId AND " +
            "(m.homePlayer.id = :playerId OR m.awayPlayer.id = :playerId)")
    List<Match> findByLeagueIdAndPlayer(@Param("leagueId") String leagueId,
                                        @Param("playerId") String playerId);

    @Query("SELECT m FROM matches m WHERE m.leagueId = :leagueId AND " +
            "(m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId)")
    List<Match> findByLeagueIdAndTeam(@Param("leagueId") String leagueId,
                                      @Param("teamId") String teamId);

//    @Query("select m from matches m where m.leagueId =:leagueId and m.result is not null")
//    List<Match> findAllPlayedLeagueMatches(@Param("leagueId") String leagueId);

    @Query("select m from matches m " +
            "where (m.homeTeam.id = :teamId or m.awayTeam.id = :teamId) " +
            "and m.status = :status " +
            "and m.leagueId in :leagueIds " +
            "order by m.roundNumber asc")
    List<Match> findByTeamStatusAndLeagueIds(@Param("teamId") String teamId,
                                             @Param("status") MatchStatus status,
                                             @Param("leagueIds") List<String> leagueIds);


    @Query("select m from matches m " +
            "where (m.homePlayer.id = :playerId or m.awayPlayer.id = :playerId) " +
            "and m.status = :status " +
            "and m.leagueId in :leagueIds " +
            "order by m.roundNumber asc")
    List<Match> findByPlayerStatusAndLeagueIds(@Param("playerId") String playerId,
                                               @Param("status") MatchStatus status,
                                               @Param("leagueIds") List<String> leagueIds);

    boolean existsByHomePlayerIdOrAwayPlayerId(String homePlayerId, String awayPlayerId);

    boolean existsByHomeTeamIdOrAwayTeamId(String homeTeamId, String awayTeamId);

    @Query("select count(m) from matches m where m.leagueId = :leagueId and m.result is not null")
    int countPlayedMatches(@Param("leagueId") String leagueId);

    int countByLeagueId(String leagueId);
}
