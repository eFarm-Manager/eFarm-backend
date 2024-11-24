package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.payload.response.CropStatisticsResponse;
import com.efarm.efarmbackend.payload.response.LandAreaStatisticsResponse;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.farmstatistics.FarmStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@RequestMapping("/statistics")
public class FarmStatisticsController {

    private final FarmStatisticsService farmStatisticsService;

    @GetMapping("/land-area")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<LandAreaStatisticsResponse> getLandAreaStatistics() {
        return ResponseEntity.ok(farmStatisticsService.generateLandAreaStatistics());
    }

    @GetMapping("/crop-area")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
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