package com.example.mauri.repository;

import com.example.mauri.enums.Sport;
import com.example.mauri.model.MatchActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MatchActivityRepository extends JpaRepository<MatchActivity, String> {

    List<MatchActivity> findBySportIsNull();

    List<MatchActivity> findBySportAndCreatedAtAfterOrderByCreatedAtDesc(Sport sport, Instant dateTime);

    long deleteByCreatedAtBefore(Instant date);

    void deleteByMatchId(String matchId);
}
