package com.example.mauri.repository;

import com.example.mauri.model.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueRepository extends JpaRepository<League, String> {

    @Query("SELECT l FROM leagues l JOIN l.players p WHERE p.id = :playerId")
    List<League> findLeaguesByPlayerId(@Param("playerId") String playerId);

    @Query("select l from leagues  l join l.teams t where t.id = :teamId")
    List<League> findLeaguesByTeamId(@Param("teamId") @NonNull String teamId);

    List<League> findAllBySeasonId(String seasonId);

//    @Query("""
//            select count (distinct p.id) from leagues l join l.players p where l.season.id = :seasonId""")
//    long countPlayersBySeasonId(@Param("seasonId") String seasonId);
//
//    @Query("""
//            select count (distinct t.id) from leagues l join l.teams t where l.season.id = :seasonId""")
//    long countTeamsBySeasonId(@Param("seasonId") String seasonId);

}
