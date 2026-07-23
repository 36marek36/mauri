package com.example.mauri.repository;

import com.example.mauri.model.VolleyMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolleyMatchRepository extends JpaRepository<VolleyMatch,String> {
    List<VolleyMatch> findByVolleyLeagueId(String leagueId);
}
