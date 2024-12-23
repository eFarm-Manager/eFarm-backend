package com.efarm.efarmbackend.service.farmstatistics;

import com.efarm.efarmbackend.payload.response.CropStatisticsResponse;
import com.efarm.efarmbackend.payload.response.LandAreaStatisticsResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public interface FarmStatisticsService {
    LandAreaStatisticsResponse generateLandAreaStatistics();

    List<CropStatisticsResponse> getCropStatistics(String seasonName) throws Exception;

    default Double roundToFourDecimalPlaces(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
