package com.example.mauri.model.dto.update;

import com.example.mauri.enums.SeasonStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSeasonDTO {
    private Integer year;
    private SeasonStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}
