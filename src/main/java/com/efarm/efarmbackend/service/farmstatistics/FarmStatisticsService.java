package com.efarm.efarmbackend.service.farmstatistics;

import com.efarm.efarmbackend.payload.response.CropStatisticsResponse;
import com.efarm.efarmbackend.payload.response.LandAreaStatisticsResponse;

import java.util.List;

public interface FarmStatisticsService {
    LandAreaStatisticsResponse generateLandAreaStatistics();

    List<CropStatisticsResponse> getCropStatistics(String seasonName) throws Exception;
}
