package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.payload.response.CropStatisticsResponse;
import com.efarm.efarmbackend.payload.response.LandAreaStatisticsResponse;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.farmstatistics.FarmStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class FarmStatisticsController {

    @Autowired
    private FarmStatisticsService farmStatisticsService;

    @GetMapping("/land-area")
    public ResponseEntity<LandAreaStatisticsResponse> getLandAreaStatistics() {
        return ResponseEntity.ok(farmStatisticsService.generateLandAreaStatistics());
    }

    @GetMapping("/crop-area")
    public ResponseEntity<?> getCropStatistics(
            @RequestParam(value = "seasonName", required = false) String seasonName) {
        try {
            List<CropStatisticsResponse> cropStatistics = farmStatisticsService.getCropStatistics(seasonName);
            return ResponseEntity.ok(cropStatistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}