package com.example.mauri.repository;

import com.example.mauri.model.VolleyLeague;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolleyLeagueRepository extends JpaRepository<VolleyLeague, String> {
}
