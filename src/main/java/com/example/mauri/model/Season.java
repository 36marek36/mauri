package com.example.mauri.model;

import com.example.mauri.enums.SeasonStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity(name = "seasons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Season {

    @Id
    private String id;

    private Integer year;

    private LocalDate createdAt;
    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("leagueType DESC, name ASC")
    @JsonManagedReference
    private List<League> leagues;

    @Enumerated(EnumType.STRING)
    private SeasonStatus status;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        status = SeasonStatus.CREATED;
    }
}
