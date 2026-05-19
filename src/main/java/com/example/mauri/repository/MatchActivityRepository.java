package com.example.mauri.repository;

import com.example.mauri.model.MatchActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MatchActivityRepository extends JpaRepository<MatchActivity, String> {

    List<MatchActivity> findByCreatedAtAfterOrderByCreatedAtDesc(Instant date);

    long deleteByCreatedAtBefore(Instant date);
}
