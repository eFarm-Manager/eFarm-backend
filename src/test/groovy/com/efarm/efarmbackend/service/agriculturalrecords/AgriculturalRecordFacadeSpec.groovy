package com.efarm.efarmbackend.service.agriculturalrecords

import com.efarm.efarmbackend.model.agriculturalrecords.*
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.landparcel.Landparcel
import com.efarm.efarmbackend.model.landparcel.LandparcelId
import com.efarm.efarmbackend.payload.request.agriculturalrecord.CreateNewAgriculturalRecordRequest
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository
import com.efarm.efarmbackend.service.landparcel.LandparcelService
import com.efarm.efarmbackend.service.user.UserService

import java.util.List
import spock.lang.Subject
import spock.lang.Specification

class AgriculturalRecordFacadeSpec extends Specification {

    SeasonService seasonService = Mock(SeasonService)
    UserService userService = Mock(UserService)
    AgriculturalRecordService agriculturalRecordService = Mock(AgriculturalRecordService)
    AgriculturalRecordRepository agriculturalRecordRepository = Mock(AgriculturalRecordRepository)
    LandparcelService landparcelService = Mock(LandparcelService)
    LandparcelRepository landparcelRepository = Mock(LandparcelRepository)

    @Subject
    AgriculturalRecordFacade agriculturalRecordFacade = new AgriculturalRecordFacade(
        seasonService: seasonService,
        userService: userService,
        agriculturalRecordService: agriculturalRecordService,
        agriculturalRecordRepository: agriculturalRecordRepository,
        landparcelService: landparcelService,
        landparcelRepository: landparcelRepository
    )

    /*
    * getAgriculturalRecords
    */
    def "should get agricultural records for current season without search query"() {
        given:
        String seasonName = null
        String searchQuery = null
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season currentSeason = Mock(Season)
        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getId() >> Mock(AgriculturalRecordId) {
                getId() >> 1
            }
            getLandparcel() >> Mock(Landparcel) {
                getName() >> 'Land 1'
                getId() >> Mock(LandparcelId) {
                    getId() >> 1
                }
            }
            getCrop() >> Mock(Crop) {
                getName() >> 'Crop 1'
            }
            getArea() >> 10.0
            getDescription() >> 'Description 1'
        }

        userService.getLoggedUserFarm() >> farm
        seasonService.getCurrentSeason() >> currentSeason
        agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(farm.getId(), currentSeason) >> [record1]
        agriculturalRecordService.filterRecordsBySearchQuery([record1], searchQuery) >> [record1]

        when:
        List<AgriculturalRecordDTO> result = agriculturalRecordFacade.getAgriculturalRecords(seasonName, searchQuery)

        then:
        result.size() == 1
        result[0].recordId == 1
        result[0].landparcelName == 'Land 1'
        result[0].landparcelId == 1
        result[0].cropName == 'Crop 1'
        result[0].area == 10.0
        result[0].description == 'Description 1'
    }

    def "should get agricultural records for specified season name"() {
        given:
        String seasonName = '2024/2025'
        String searchQuery = null
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season specifiedSeason = Mock(Season)
        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getId() >> Mock(AgriculturalRecordId) {
                getId() >> 1
            }
            getLandparcel() >> Mock(Landparcel) {
                getName() >> 'Land 1'
                getId() >> Mock(LandparcelId) {
                    getId() >> 1
                }
            }
            getCrop() >> Mock(Crop) {
                getName() >> 'Crop 1'
            }
            getArea() >> 10.0
            getDescription() >> 'Description 1'
        }

        userService.getLoggedUserFarm() >> farm
        seasonService.getSeasonByName(seasonName) >> specifiedSeason
        agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(farm.getId(), specifiedSeason) >> [record1]
        agriculturalRecordService.filterRecordsBySearchQuery([record1], searchQuery) >> [record1]

        when:
        List<AgriculturalRecordDTO> result = agriculturalRecordFacade.getAgriculturalRecords(seasonName, searchQuery)

        then:
        result.size() == 1
        result[0].recordId == 1
        result[0].landparcelName == 'Land 1'
        result[0].landparcelId == 1
        result[0].cropName == 'Crop 1'
        result[0].area == 10.0
        result[0].description == 'Description 1'
    }

    def "should filter agricultural records by search query"() {
        given:
        String seasonName = null
        String searchQuery = 'Crop 1'
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season currentSeason = Mock(Season)
        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getId() >> Mock(AgriculturalRecordId) {
                getId() >> 1
            }
            getLandparcel() >> Mock(Landparcel) {
                getName() >> 'Land 1'
                getId() >> Mock(LandparcelId) {
                    getId() >> 1
                }
            }
            getCrop() >> Mock(Crop) {
                getName() >> 'Crop 1'
            }
            getArea() >> 10.0
            getDescription() >> 'Description 1'
        }
        AgriculturalRecord record2 = Mock(AgriculturalRecord) {
            getId() >> Mock(AgriculturalRecordId) {
                getId() >> 2
            }
            getLandparcel() >> Mock(Landparcel) {
                getName() >> 'Land 2'
                getId() >> Mock(LandparcelId) {
                    getId() >> 2
                }
            }
            getCrop() >> Mock(Crop) {
                getName() >> 'Crop 2'
            }
            getArea() >> 5.0
            getDescription() >> 'Description 2'
        }

        userService.getLoggedUserFarm() >> farm
        seasonService.getCurrentSeason() >> currentSeason
        agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(farm.getId(), currentSeason) >> [record1, record2]
        agriculturalRecordService.filterRecordsBySearchQuery([record1, record2], searchQuery) >> [record1]

        when:
        List<AgriculturalRecordDTO> result = agriculturalRecordFacade.getAgriculturalRecords(seasonName, searchQuery)

        then:
        result.size() == 1
        result[0].recordId == 1
        result[0].landparcelName == 'Land 1'
        result[0].landparcelId == 1
        result[0].cropName == 'Crop 1'
        result[0].area == 10.0
        result[0].description == 'Description 1'
    }

    def "should handle no records found"() {
        given:
        String seasonName = null
        String searchQuery = null
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season currentSeason = Mock(Season)

        userService.getLoggedUserFarm() >> farm
        seasonService.getCurrentSeason() >> currentSeason
        agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(farm.getId(), currentSeason) >> []
        agriculturalRecordService.filterRecordsBySearchQuery([], searchQuery) >> []

        when:
        List<AgriculturalRecordDTO> result = agriculturalRecordFacade.getAgriculturalRecords(seasonName, searchQuery)

        then:
        result.isEmpty()
    }

    /*
    * addAgriculturalRecord
    */

    def "should add new agricultural record"() {
        given:
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest(
            landparcelId: 1,
            cropName: 'Wheat',
            area: 10.0,
            description: 'Description 1'
        )
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season season = Mock(Season)
        Landparcel landparcel = Mock(Landparcel) {
            getId() >> Mock(LandparcelId) {
                getId() >> 1
            }
            getIsAvailable() >> true
        }
        Crop crop = Mock(Crop)

        userService.getLoggedUserFarm() >> farm
        seasonService.getCurrentSeason() >> season
        landparcelService.findlandparcelByFarm(request.getLandparcelId(), farm) >> landparcel
        agriculturalRecordService.validateCropArea(landparcel, season, request) >> { }
        agriculturalRecordService.validateCrop(landparcel, season, request.getCropName()) >> crop
        agriculturalRecordRepository.findNextFreeIdForFarm(farm.getId()) >> 1

        when:
        agriculturalRecordFacade.addAgriculturalRecord(request)

        then:
        1 * agriculturalRecordRepository.save({ it instanceof AgriculturalRecord })
    }

    def "should add new agricultural record with specified season"() {
        given:
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest(
            landparcelId: 1,
            season: '2022/2023',
            cropName: 'Wheat',
            area: 10.0,
            description: 'Description 1'
        )
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season specifiedSeason = Mock(Season)
        Landparcel landparcel = Mock(Landparcel) {
            getIsAvailable() >> true
        }
        Crop crop = Mock(Crop)

        userService.getLoggedUserFarm() >> farm
        seasonService.getSeasonByName(request.getSeason()) >> specifiedSeason
        landparcelService.findlandparcelByFarm(request.getLandparcelId(), farm) >> landparcel
        agriculturalRecordService.validateCropArea(landparcel, specifiedSeason, request) >> { }
        agriculturalRecordService.validateCrop(landparcel, specifiedSeason, request.getCropName()) >> crop
        agriculturalRecordRepository.findNextFreeIdForFarm(farm.getId()) >> 1

        when:
        agriculturalRecordFacade.addAgriculturalRecord(request)

        then:
        1 * agriculturalRecordRepository.save({ it instanceof AgriculturalRecord })
    }

    def "should throw exception when crop area validation fails"() {
        given:
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest(
            landparcelId: 1,
            cropName: 'Wheat',
            area: 20.0,
            description: 'Description 1'
        )
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season season = Mock(Season)
        Landparcel landparcel = Mock(Landparcel) {
            getIsAvailable() >> true
        }

        userService.getLoggedUserFarm() >> farm
        seasonService.getCurrentSeason() >> season
        landparcelService.findlandparcelByFarm(request.getLandparcelId(), farm) >> landparcel
        agriculturalRecordService.validateCropArea(landparcel, season, request) >> { throw new Exception('Crop area exceeds limit') }

        when:
        agriculturalRecordFacade.addAgriculturalRecord(request)

        then:
        Exception e = thrown()
        e.message == 'Crop area exceeds limit'
    }

    /*
    * createRecordsForNewSeason
    */

    def "should create records for new season"() {
        given:
        String seasonName = '2024/2025'
        Farm farm = Mock(Farm)
        Season season = Mock(Season)
        Landparcel landparcel1 = Mock(Landparcel)
        Landparcel landparcel2 = Mock(Landparcel)

        userService.getLoggedUserFarm() >> farm
        seasonService.getSeasonByName(seasonName) >> season
        landparcelRepository.findByFarmIdAndIsAvailableTrue(farm.getId()) >> [landparcel1, landparcel2]

        when:
        agriculturalRecordFacade.createRecordsForNewSeason(seasonName)

        then:
        1 * agriculturalRecordService.createAgriculturalRecordForLandparcel(landparcel1, farm, season)
        1 * agriculturalRecordService.createAgriculturalRecordForLandparcel(landparcel2, farm, season)
    }

    def "should throw exception when season does not exist"() {
        given:
        String seasonName = '2024/2025'
        Farm farm = Mock(Farm)

        userService.getLoggedUserFarm() >> farm
        seasonService.getSeasonByName(seasonName) >> null

        when:
        agriculturalRecordFacade.createRecordsForNewSeason(seasonName)

        then:
        Exception e = thrown()
        e.message == 'Podany sezon nie istnieje.'
    }

    def "should not create records if no active land parcels are available"() {
        given:
        String seasonName = '2024/2025'
        Farm farm = Mock(Farm)
        Season season = Mock(Season)

        userService.getLoggedUserFarm() >> farm
        seasonService.getSeasonByName(seasonName) >> season
        landparcelRepository.findByFarmIdAndIsAvailableTrue(farm.getId()) >> []

        when:
        agriculturalRecordFacade.createRecordsForNewSeason(seasonName)

        then:
        0 * agriculturalRecordService.createAgriculturalRecordForLandparcel(_, _, _)
    }

}
