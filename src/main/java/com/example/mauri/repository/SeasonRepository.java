package com.example.mauri.repository;

import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, String> {
    Optional<Season> findByStatus(SeasonStatus status);
}
