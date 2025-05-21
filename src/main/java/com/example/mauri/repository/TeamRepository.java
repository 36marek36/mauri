package com.example.mauri.repository;

import com.example.mauri.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {

    @Query("select t from teams t where t not in (select ts from leagues l join l.teams ts)")
    List<Team> findTeamsNotInAnyLeague();
}
