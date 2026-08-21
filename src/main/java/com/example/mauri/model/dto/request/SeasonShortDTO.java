package com.example.mauri.model.dto.request;

import com.example.mauri.enums.SeasonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeasonShortDTO {
    private String id;
    private int year;
    private SeasonStatus status;
}
