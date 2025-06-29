package com.example.mauri.repository;

import com.example.mauri.model.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueRepository extends JpaRepository<League, String> {

    List<League> findBySeasonIsNull();

    @Query("SELECT l FROM leagues l JOIN l.players p WHERE p.id = :playerId")
    List<League> findLeaguesByPlayerId(@Param("playerId") String playerId);

}
