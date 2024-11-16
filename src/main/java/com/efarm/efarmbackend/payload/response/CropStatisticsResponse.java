package com.efarm.efarmbackend.payload.response;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class CropStatisticsResponse {
    private String cropName;
    private Double totalArea;
}

