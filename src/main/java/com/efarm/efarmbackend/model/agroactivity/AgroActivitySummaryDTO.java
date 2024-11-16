package com.efarm.efarmbackend.model.agroactivity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class AgroActivitySummaryDTO {
    private Integer id;
    private String name;
    private Instant date;
    private Boolean isCompleted;
    private String categoryName;

    public AgroActivitySummaryDTO(AgroActivity agroActivity) {
        this.id = agroActivity.getId().getId();
        this.name = agroActivity.getName();
        this.date = agroActivity.getDate();
        this.isCompleted = agroActivity.getIsCompleted();
        this.categoryName = agroActivity.getActivityCategory().getName();
    }
}

