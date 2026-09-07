package com.example.mauri.repository;

import com.example.mauri.enums.Sport;
import com.example.mauri.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {

    @Query("SELECT p FROM players p WHERE p.active = true AND p.id NOT IN (SELECT u.player.id FROM users u WHERE u.player IS NOT NULL)")
    List<Player> findActivePlayersWithoutUser();

    List<Player> findByActiveTrueAndSportsContainingOrderByLastNameAsc(Sport sport);

    List<Player> findByActiveFalseAndSportsContainingOrderByLastNameAsc(Sport sport);

    boolean existsByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT p FROM players p WHERE p.active = true " +
            "AND :sport MEMBER OF p.sports " +
            "AND p.id NOT IN (SELECT pl.id FROM leagues l JOIN l.players pl WHERE l.id = :leagueId) " +
            "ORDER BY p.lastName ASC")
    List<Player> findPlayersNotInLeagueBySport(
            @Param("leagueId") String leagueId,
            @Param("sport") Sport sport
    );

}
