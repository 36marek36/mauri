package com.example.mauri.service;

import com.example.mauri.model.Season;
import com.example.mauri.model.dto.create.CreateSeasonDTO;
import com.example.mauri.model.dto.response.SeasonDTO;
import lombok.NonNull;

import java.util.List;

public interface SeasonService {
    List<SeasonDTO> getSeasons();
//    Season getSeason(@NonNull String id);
    Season createSeason(CreateSeasonDTO createSeasonDTO);
    void deleteSeason(@NonNull String id);
    Season addLeagueToSeason(@NonNull String leagueId, @NonNull String seasonId);
    String startSeason(String seasonId);
    String finishSeason(String seasonId);
    SeasonDTO getSeasonStats(String seasonId);
}
