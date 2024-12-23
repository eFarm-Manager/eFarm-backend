package com.efarm.efarmbackend.service.farmstatistics

import com.efarm.efarmbackend.model.agriculturalrecords.Season
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus
import com.efarm.efarmbackend.payload.response.CropStatisticsResponse
import com.efarm.efarmbackend.payload.response.LandAreaStatisticsResponse
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository
import com.efarm.efarmbackend.service.agriculturalrecords.SeasonService
import com.efarm.efarmbackend.service.user.UserService
import spock.lang.Specification
import spock.lang.Subject

class FarmStatisticsServiceSpec extends Specification {

    def userService = Mock(UserService)
    def landparcelRepository = Mock(LandparcelRepository)
    def agriculturalRecordRepository = Mock(AgriculturalRecordRepository)
    def seasonService = Mock(SeasonService)

    @Subject
    FarmStatisticsService farmStatisticsService = new FarmStatisticsService(
            userService,
            landparcelRepository,
            agriculturalRecordRepository,
            seasonService
    )

    def "should generate land area statistics"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }

        LandAreaStatisticsResponse response = new LandAreaStatisticsResponse(
                totalAvailableArea: BigDecimal.valueOf(100),
                privatelyOwnedArea: BigDecimal.valueOf(75),
                leaseArea: BigDecimal.valueOf(25)
        )

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.sumAvailableLandArea(farm.getId()) >> BigDecimal.valueOf(100)
        landparcelRepository.sumAvailableLandAreaByStatus(farm.getId(), ELandOwnershipStatus.STATUS_PRIVATELY_OWNED) >> BigDecimal.valueOf(75)
        landparcelRepository.sumAvailableLandAreaByStatus(farm.getId(), ELandOwnershipStatus.STATUS_LEASE) >> BigDecimal.valueOf(25)

        when:
        LandAreaStatisticsResponse result = farmStatisticsService.generateLandAreaStatistics()

        then:
        result.getTotalAvailableArea() == response.getTotalAvailableArea()
    }

    def "should generate crop statistics"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }

        List<CropStatisticsResponse> responses = [
                new CropStatisticsResponse(cropName: 'crop1', totalArea: 50.0),
                new CropStatisticsResponse(cropName: 'crop2', totalArea: 30.0),
                new CropStatisticsResponse(cropName: 'crop3', totalArea: 20.0)
        ]

        Season season = Mock(Season) {
            getId() >> 1
            getName() >> '2024/2025'
        }

        seasonService.getSeasonByName(season.getName()) >> season
        userService.getLoggedUserFarm() >> farm
        agriculturalRecordRepository.findCropStatisticsBySeasonAndFarm(season.getId(), farm.getId()) >> [
                ['crop1', 50.0d] as Object[],
                ['crop2', 30.0d] as Object[],
                ['crop3', 20.0d] as Object[]
        ]

        when:
        List<CropStatisticsResponse> result = farmStatisticsService.getCropStatistics(season.getName())

        then:
        result.size() == responses.size()
        result*.cropName == responses*.cropName
        result*.totalArea == responses*.totalArea
    }

    // 5 is in 5th decimal place, so it should be rounded up
    def "should correctly round up to four decimal places"() {
        expect:
        farmStatisticsService.roundToFourDecimalPlaces(1.123456789d) == 1.1235d
    }

    // 4 is in 5th decimal place, so it should be rounded down
    def "should correctly round down to four decimal places"() {
        expect:
        farmStatisticsService.roundToFourDecimalPlaces(1.123446789d) == 1.1234d
    }

}
