package com.efarm.efarmbackend.model.agriculturalrecords;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgriculturalRecordDTO {
    private Integer recordId;
    private String landparcelName;
    private Integer landparcelId;
    private String cropName;
    private double area;
    private String description;
}

