package com.example.mauri.service;

import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.model.dto.create.CreateSeasonDTO;
import com.example.mauri.model.dto.response.SeasonResponseDTO;
import com.example.mauri.model.dto.response.TennisSeasonListResponseDTO;
import com.example.mauri.model.dto.response.VolleySeasonListResponseDTO;
import com.example.mauri.model.dto.update.UpdateSeasonDTO;
import lombok.NonNull;

import java.util.List;

public interface SeasonService {
    List<SeasonResponseDTO> getSeasons();

    SeasonResponseDTO createSeason(CreateSeasonDTO createSeasonDTO);

    void deleteSeason(@NonNull String id);

    String addLeagueToSeason(@NonNull String leagueId, @NonNull String seasonId);

    String startSeason(String seasonId);

    String finishSeason(String seasonId);

    SeasonResponseDTO getSeasonStats(String seasonId);

    SeasonResponseDTO updateSeason(String seasonId, UpdateSeasonDTO updateSeasonDTO);

    boolean isSeasonActive();

    List<TennisSeasonListResponseDTO> getTennisSeasons(List<SeasonStatus> statuses);
    List<VolleySeasonListResponseDTO> getVolleySeasons(List<SeasonStatus> statuses);

}
