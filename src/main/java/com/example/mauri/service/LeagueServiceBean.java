package com.example.mauri.service;

import com.example.mauri.model.League;
import com.example.mauri.model.dto.CreateLeagueDTO;
import com.example.mauri.repository.LeagueRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LeagueServiceBean implements LeagueService {

    private final LeagueRepository leagueRepository;

    public LeagueServiceBean(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Override
    public List<League> getAllLeagues() {
        return leagueRepository.findAll().stream().toList();
    }

    @Override
    public League getLeagueById(@NonNull String id) {
        return leagueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No league found with id: " + id));
    }

    @Override
    public League createLeague(CreateLeagueDTO createLeagueDTO) {
        League league = new League(
                UUID.randomUUID().toString(),
                createLeagueDTO.getLeagueName(),
                createLeagueDTO.getLeagueType(),
                new ArrayList<>(),
                new ArrayList<>());
        return leagueRepository.save(league);
    }

    @Override
    public void deleteLeagueById(@NonNull String id) {
        if (!leagueRepository.existsById(id)) {
            throw new IllegalArgumentException("No league found with id: " + id);
        }
        leagueRepository.deleteById(id);
    }

}
