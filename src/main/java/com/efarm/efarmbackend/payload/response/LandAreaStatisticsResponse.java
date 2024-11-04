package com.efarm.efarmbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LandAreaStatisticsResponse {
    private Double totalAvailableArea;
    private Double privatelyOwnedArea;
    private Double leaseArea;
}