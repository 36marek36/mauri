package com.example.mauri.repository;

import com.example.mauri.model.VolleyMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolleyMatchRepository extends JpaRepository<VolleyMatch,String> {
}
