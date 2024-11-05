package com.efarm.efarmbackend.model.agroactivity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AgroActivitySummaryDTO {
    private Integer id;
    private String name;
    private Instant date;
    private Boolean isCompleted;
    private String categoryName;
}

