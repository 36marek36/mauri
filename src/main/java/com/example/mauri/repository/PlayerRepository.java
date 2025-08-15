package com.example.mauri.repository;

import com.example.mauri.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {
//    @Query("SELECT p FROM players p WHERE p NOT IN (SELECT pl FROM leagues l JOIN l.players pl)")
//    List<Player> findPlayersNotInAnyLeague();

//    @Query("""
//                SELECT p
//                FROM players p
//                WHERE p NOT IN (
//                    SELECT pl
//                    FROM leagues l
//                    JOIN l.players pl
//                    WHERE l.status = 'CREATED' or l.status = 'ACTIVE'
//                )
//            """)
//    List<Player> findPlayersNotInAnyActiveLeague();

    @Query("SELECT p FROM players p WHERE p.active = true AND p.id NOT IN (SELECT pl.id FROM leagues l JOIN l.players pl WHERE l.status = 'CREATED' or l.status = 'ACTIVE')")
    List<Player> findActivePlayersWithoutActiveLeague();

    @Query("SELECT p FROM players p WHERE p.active = true AND p.id NOT IN (SELECT u.player.id FROM users u WHERE u.player IS NOT NULL)")
    List<Player> findActivePlayersWithoutUser();

    List<Player> findByActiveTrue();
    List<Player> findByActiveFalse();

}
