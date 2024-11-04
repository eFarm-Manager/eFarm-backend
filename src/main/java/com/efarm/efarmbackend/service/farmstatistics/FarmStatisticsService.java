package com.efarm.efarmbackend.service.farmstatistics;

import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus;
import com.efarm.efarmbackend.payload.response.CropStatisticsResponse;
import com.efarm.efarmbackend.payload.response.LandAreaStatisticsResponse;
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository;
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import com.efarm.efarmbackend.service.agriculturalrecords.SeasonService;
import com.efarm.efarmbackend.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FarmStatisticsService {

    @Autowired
    private UserService userService;

    @Autowired
    private LandparcelRepository landparcelRepository;

    @Autowired
    private AgriculturalRecordRepository agriculturalRecordRepository;

    @Autowired
    private SeasonService seasonService;

    public LandAreaStatisticsResponse generateLandAreaStatistics() {
        Farm loggedUserFarm = userService.getLoggedUserFarm();

        Double totalAvailableArea = landparcelRepository.sumAvailableLandArea(loggedUserFarm.getId());
        Double privatelyOwnedAvailableArea = landparcelRepository.sumAvailableLandAreaByStatus(loggedUserFarm.getId(), ELandOwnershipStatus.STATUS_PRIVATELY_OWNED);
        Double leaseAvailableArea = landparcelRepository.sumAvailableLandAreaByStatus(loggedUserFarm.getId(), ELandOwnershipStatus.STATUS_LEASE);

        totalAvailableArea = roundToTwoDecimalPlaces(totalAvailableArea);
        privatelyOwnedAvailableArea = roundToTwoDecimalPlaces(privatelyOwnedAvailableArea);
        leaseAvailableArea = roundToTwoDecimalPlaces(leaseAvailableArea);

        return new LandAreaStatisticsResponse(totalAvailableArea, privatelyOwnedAvailableArea, leaseAvailableArea);
    }

    public List<CropStatisticsResponse> getCropStatistics(String seasonName) throws Exception {

        Season season = (seasonName != null) ? seasonService.getSeasonByName(seasonName) : seasonService.getCurrentSeason();
        List<Object[]> cropStatistics = agriculturalRecordRepository.findCropStatisticsBySeasonAndFarm(season.getId(), userService.getLoggedUserFarm().getId());

        return cropStatistics.stream()
                .map(result -> new CropStatisticsResponse(
                        (String) result[0],
                        roundToTwoDecimalPlaces((Double) result[1])
                ))
                .sorted((a, b) -> b.getTotalArea().compareTo(a.getTotalArea()))
                .collect(Collectors.toList());
    }

    private Double roundToTwoDecimalPlaces(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
