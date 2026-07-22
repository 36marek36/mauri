package com.example.mauri.repository;

import com.example.mauri.model.VolleyTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolleyTeamRepository extends JpaRepository<VolleyTeam, String> {
    boolean existsByName(String teamName);

}
