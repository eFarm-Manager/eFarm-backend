package com.efarm.efarmbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class CropStatisticsResponse {
    private String cropName;
    private Double totalArea;
}

