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
    private Integer id;
    private String landparcelName;
    private String cropName;
    private double area;
}

