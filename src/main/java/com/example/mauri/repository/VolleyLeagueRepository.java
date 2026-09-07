package com.example.mauri.repository;

import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.model.VolleyLeague;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface VolleyLeagueRepository extends JpaRepository<VolleyLeague, String> {

    @Query("SELECT v FROM volleyball_leagues v join v.teams t WHERE t.id = :teamId")
    List<VolleyLeague> findVolleyLeagueByVolleyTeamId(@Param("teamId") @NonNull String teamId);
    List<VolleyLeague> findAllBySeasonStatus (SeasonStatus status);

    @Query("""
    SELECT DISTINCT vl
    FROM volleyball_leagues vl
    LEFT JOIN FETCH vl.teams
    WHERE vl.season.id IN :seasonIds
""")
    List<VolleyLeague> findBySeasonIdInWithTeams(
            @Param("seasonIds") Set<String> seasonIds
    );
}
