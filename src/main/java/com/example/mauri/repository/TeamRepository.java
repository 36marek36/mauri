package com.example.mauri.repository;

import com.example.mauri.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {

    @Query("select t from teams t where  t.active = true and  t.id not in (" +
            "select ts.id from leagues l join l.teams ts where l.status ='CREATED' or l.status = 'ACTIVE')")
    List<Team> findActiveTeamsWithoutActiveLeague();

    boolean existsByPlayer1IdOrPlayer2Id(String player1Id, String player2Id);

    List<Team> findByPlayer1IdOrPlayer2Id(String player1Id, String player2Id);

    List<Team> findByActiveTrue();

    List<Team> findByActiveFalse();

    @Query("SELECT t FROM teams t WHERE t.active = true and t.id NOT IN (SELECT tm.id FROM leagues l join l.teams tm WHERE l.id = :leagueId)")
    List<Team> findTeamsNotInLeague(@Param("leagueId") String leagueId);
}
