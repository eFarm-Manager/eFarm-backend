package com.efarm.efarmbackend.model.agriculturalrecords;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgriculturalRecordDTO {
    private Integer recordId;
    private String landparcelName;
    private Integer landparcelId;
    private String cropName;
    private double area;
    private String description;

    public AgriculturalRecordDTO(AgriculturalRecord record) {
        this.recordId = record.getId().getId();
        this.landparcelName = record.getLandparcel().getName();
        this.landparcelId = record.getLandparcel().getId().getId();
        this.cropName = record.getCrop().getName();
        this.area = record.getArea();
        this.description = record.getDescription();
    }
}