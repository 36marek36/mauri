package com.example.mauri.repository;

import com.example.mauri.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {
    @Query("SELECT p FROM players p WHERE p NOT IN (SELECT pl FROM leagues l JOIN l.players pl)")
    List<Player> findPlayersNotInAnyLeague();

}
