package com.efarm.efarmbackend.service.agriculturalrecords

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId
import com.efarm.efarmbackend.model.agriculturalrecords.Crop
import com.efarm.efarmbackend.model.agriculturalrecords.Season
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.landparcel.Landparcel
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId;
import com.efarm.efarmbackend.repository.agroactivity.AgroActivityRepository;
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import com.efarm.efarmbackend.service.agroactivity.AgroActivityService;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.CreateNewAgriculturalRecordRequest
import com.efarm.efarmbackend.payload.request.agriculturalrecord.UpdateAgriculturalRecordRequest
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository
import com.efarm.efarmbackend.repository.agriculturalrecords.CropRepository
import com.efarm.efarmbackend.service.user.UserService

import java.util.List
import spock.lang.Subject
import spock.lang.Specification

class AgriculturalRecordServiceSpec extends Specification {

    def agriculturalRecordRepository = Mock(AgriculturalRecordRepository)
    def cropRepository = Mock(CropRepository)
    def landparcelRepository = Mock(LandparcelRepository)
    def userService = Mock(UserService)
    def agroActivityService = Mock(AgroActivityService)
    def agroActivityRepository = Mock(AgroActivityRepository)

    @Subject
    AgriculturalRecordService agriculturalRecordService = new AgriculturalRecordService(
            agriculturalRecordRepository: agriculturalRecordRepository,
            cropRepository: cropRepository,
            landparcelRepository: landparcelRepository,
            userService: userService,
            agroActivityService: agroActivityService,
            agroActivityRepository: agroActivityRepository
    )

    /*
    * filterRecordsBySearchQuery
    */

    def "should filter records by search query of landparcel name"() {
        given:
        String searchQuery = 'Field A'
        Landparcel landparcel1 = new Landparcel(name: 'Field A')
        Landparcel landparcel2 = new Landparcel(name: 'Field B')
        Crop crop1 = new Crop(name: 'Wheat')
        Crop crop2 = new Crop(name: 'Corn')

        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop1
        }
        AgriculturalRecord record2 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel2
            getCrop() >> crop2
        }
        AgriculturalRecord record3 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop2
        }

        List<AgriculturalRecord> records = [record1, record2, record3]

        when:
        List<AgriculturalRecord> filteredRecords = agriculturalRecordService.filterRecordsBySearchQuery(records, searchQuery)

        then:
        filteredRecords.size() == 2
        filteredRecords.contains(record1)
        filteredRecords.contains(record3)
    }

    def "should filter records by search query of crop name"() {
        given:
        String searchQuery = 'Wheat'
        Landparcel landparcel1 = new Landparcel(name: 'Field A')
        Landparcel landparcel2 = new Landparcel(name: 'Field B')
        Crop crop1 = new Crop(name: 'Wheat')
        Crop crop2 = new Crop(name: 'Corn')
        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop1
        }
        AgriculturalRecord record2 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel2
            getCrop() >> crop2
        }
        AgriculturalRecord record3 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop2
        }
        List<AgriculturalRecord> records = [record1, record2, record3]
        when:
        List<AgriculturalRecord> filteredRecords = agriculturalRecordService.filterRecordsBySearchQuery(records, searchQuery)
        then:
        filteredRecords.size() == 1
        filteredRecords.contains(record1)
    }

    def "should filter records by search query null"() {
        given:
        Landparcel landparcel1 = new Landparcel(name: 'Field A')
        Landparcel landparcel2 = new Landparcel(name: 'Field B')
        Crop crop1 = new Crop(name: 'Wheat')
        Crop crop2 = new Crop(name: 'Corn')

        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop1
        }
        AgriculturalRecord record2 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel2
            getCrop() >> crop2
        }
        AgriculturalRecord record3 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop2
        }

        List<AgriculturalRecord> records = [record1, record2, record3]

        when:
        List<AgriculturalRecord> filteredRecords = agriculturalRecordService.filterRecordsBySearchQuery(records, null)

        then:
        filteredRecords.size() == 3
        filteredRecords.contains(record1)
        filteredRecords.contains(record2)
        filteredRecords.contains(record3)
    }

    def "should filter records by empty search query"() {
        given:
        String searchQuery = ''
        Landparcel landparcel1 = new Landparcel(name: 'Field A')
        Landparcel landparcel2 = new Landparcel(name: 'Field B')
        Crop crop1 = new Crop(name: 'Wheat')
        Crop crop2 = new Crop(name: 'Corn')

        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop1
        }
        AgriculturalRecord record2 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel2
            getCrop() >> crop2
        }
        AgriculturalRecord record3 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop2
        }

        List<AgriculturalRecord> records = [record1, record2, record3]

        when:
        List<AgriculturalRecord> filteredRecords = agriculturalRecordService.filterRecordsBySearchQuery(records, searchQuery)

        then:
        filteredRecords.size() == 3
        filteredRecords.containsAll(records)
    }

    def "should return empty list if no records match the search query"() {
        given:
        String searchQuery = 'Rice'
        Landparcel landparcel1 = new Landparcel(name: 'Field A')
        Landparcel landparcel2 = new Landparcel(name: 'Field B')
        Crop crop1 = new Crop(name: 'Wheat')
        Crop crop2 = new Crop(name: 'Corn')

        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop1
        }
        AgriculturalRecord record2 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel2
            getCrop() >> crop2
        }
        AgriculturalRecord record3 = Mock(AgriculturalRecord) {
            getLandparcel() >> landparcel1
            getCrop() >> crop2
        }

        List<AgriculturalRecord> records = [record1, record2, record3]

        when:
        List<AgriculturalRecord> filteredRecords = agriculturalRecordService.filterRecordsBySearchQuery(records, searchQuery)

        then:
        filteredRecords.isEmpty()
    }
    /*
    * getAgriculturalRecordsForFarmAndSeason
    */

    def "should get agricultural records for farm and season"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season season = Mock(Season)
        Landparcel landparcel = Mock(Landparcel) {
            getIsAvailable() >> true
        }
        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getId() >> 1
            getFarm() >> farm
            getSeason() >> season
            getLandparcel() >> landparcel
        }
        AgriculturalRecord record2 = Mock(AgriculturalRecord) {
            getId() >> 2
            getFarm() >> farm
            getSeason() >> season
            getLandparcel() >> landparcel
        }

        landparcelRepository.findByFarmId(farm.getId()) >> [landparcel]
        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >> [record1, record2]

        when:
        List<AgriculturalRecord> records = agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(farm.getId(), season)

        then:
        records.size() == 2
        records.contains(record1)
        records.contains(record2)
    }

    def "should return records for multiple available landparcels"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season season = Mock(Season)
        Landparcel landparcel1 = Mock(Landparcel) {
            getIsAvailable() >> true
        }
        Landparcel landparcel2 = Mock(Landparcel) {
            getIsAvailable() >> true
        }

        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getId() >> 1
            getFarm() >> farm
            getSeason() >> season
            getLandparcel() >> landparcel1
        }
        AgriculturalRecord record2 = Mock(AgriculturalRecord) {
            getId() >> 2
            getFarm() >> farm
            getSeason() >> season
            getLandparcel() >> landparcel2
        }

        landparcelRepository.findByFarmId(farm.getId()) >> [landparcel1, landparcel2]
        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel1, season) >> [record1]
        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel2, season) >> [record2]

        when:
        List<AgriculturalRecord> records = agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(farm.getId(), season)

        then:
        records.size() == 2
        records.contains(record1)
        records.contains(record2)
    }

    def "should ignore unavailable landparcels"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season season = Mock(Season)
        Landparcel landparcel1 = Mock(Landparcel) {
            getIsAvailable() >> true // Available landparcel
        }
        Landparcel landparcel2 = Mock(Landparcel) {
            getIsAvailable() >> false // Unavailable landparcel
        }

        AgriculturalRecord record1 = Mock(AgriculturalRecord) {
            getId() >> 1
            getFarm() >> farm
            getSeason() >> season
            getLandparcel() >> landparcel1
        }

        landparcelRepository.findByFarmId(farm.getId()) >> [landparcel1, landparcel2]
        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel1, season) >> [record1]
        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel2, season) >> []

        when:
        List<AgriculturalRecord> records = agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(farm.getId(), season)

        then:
        records.size() == 1
        records.contains(record1)
    }

    def "should return empty list if no available landparcels exist"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season season = Mock(Season)

        landparcelRepository.findByFarmId(farm.getId()) >> []

        when:
        List<AgriculturalRecord> records = agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(farm.getId(), season)

        then:
        records.isEmpty()
    }

    def "should return empty list if available landparcels have no agricultural records"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Season season = Mock(Season)
        Landparcel landparcel = Mock(Landparcel) {
            getIsAvailable() >> true
        }

        landparcelRepository.findByFarmId(farm.getId()) >> [landparcel]
        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >> []

        when:
        List<AgriculturalRecord> records = agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(farm.getId(), season)

        then:
        records.isEmpty()
    }

    /*
    * validateCrop
    */

    def "should validate crop correctly"() {
        given:
        Boolean showAdditionalInfo = true
        String cropName = 'Wheat'
        Crop crop = Mock(Crop) {
            getName() >> cropName
        }
        Landparcel landparcel = Mock(Landparcel)
        Season season = Mock(Season)

        cropRepository.findByName(cropName) >> crop
        agriculturalRecordRepository.findByLandparcelAndSeasonAndCrop(landparcel, season, crop) >> []

        when:
        Crop validatedCrop = agriculturalRecordService.validateCrop(landparcel, season, cropName,showAdditionalInfo)

        then:
        validatedCrop == crop
    }

    def "should throw exception if crop is not found"() {
        given:
        Boolean showAdditionalInfo = true
        String cropName = 'Rice'
        Landparcel landparcel = Mock(Landparcel)
        Season season = Mock(Season)

        cropRepository.findByName(cropName) >> null

        when:
        agriculturalRecordService.validateCrop(landparcel, season, cropName, showAdditionalInfo)

        then:
        Exception exception = thrown(Exception)
        exception.message == 'Wybrano nieprawidłowy rodzaj uprawy'
    }

    def "should throw exception if crop already exists on landparcel wtih additonal info"() {
        given:
        Boolean showAdditionalInfo = true
        String cropName = 'Wheat'
        Crop crop = Mock(Crop) {
            getName() >> cropName
        }
        Landparcel landparcel = Mock(Landparcel)
        Season season = Mock(Season)

        cropRepository.findByName(cropName) >> crop 
        agriculturalRecordRepository.findByLandparcelAndSeasonAndCrop(landparcel, season, crop) >> [Mock(AgriculturalRecord)]

        when:
        agriculturalRecordService.validateCrop(landparcel, season, cropName, showAdditionalInfo)

        then:
        Exception exception = thrown(Exception)
        exception.message == 'Wybrana uprawa już istnieje na tym polu. Możesz zmienić jej powierzchnię zamiast dodawać ją ponownie.'
    }

    def "should throw exception if crop already exists on landparcel wtihout additonal info"() {
        given:
        Boolean showAdditionalInfo = false
        String cropName = 'Wheat'
        Crop crop = Mock(Crop) {
            getName() >> cropName
        }
        Landparcel landparcel = Mock(Landparcel)
        Season season = Mock(Season)

        cropRepository.findByName(cropName) >> crop 
        agriculturalRecordRepository.findByLandparcelAndSeasonAndCrop(landparcel, season, crop) >> [Mock(AgriculturalRecord)]

        when:
        agriculturalRecordService.validateCrop(landparcel, season, cropName, showAdditionalInfo)

        then:
        Exception exception = thrown(Exception)
        exception.message == 'Wybrana uprawa już istnieje na tym polu'
    }
    /*
    * validateCropArea
    */

    def "should vaildate crop area successfuly if there are no existing records"() {
        given:
        Landparcel landparcel = Mock(Landparcel) {
            getArea() >> 30.0
        }
        Season season = Mock(Season)
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest(
            landparcelId: 1,
            season: '',
            cropName: 'Wheat',
            area: 10.0,
            description: ''
        )

        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >> []

        when:
        agriculturalRecordService.validateCropArea(landparcel, season, request)

        then:
        noExceptionThrown()
    }

    def "should throw exception if requested area exceeds available space"() {
        given:
        Landparcel landparcel = Mock(Landparcel) {
            getArea() >> 30.0
        }
        Season season = Mock(Season)
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest(
            landparcelId: 1,
            season: '',
            cropName: 'Wheat',
            area: 25.0,
            description: ''
        )

        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >>
            [Mock(AgriculturalRecord) {
                getArea() >> 10.0
            }]

        when:
        agriculturalRecordService.validateCropArea(landparcel, season, request)

        then:
        Exception exception = thrown(Exception)
        exception.message == 'Maksymalna niewykorzystana powierzchnia na tym polu to: 20.0 ha. Spróbuj najpierw zmniejszyć powierzchnię pozostałych upraw'
    }

    def "should validate successfully if requested area exactly matches remaining space"() {
        given:
        Landparcel landparcel = Mock(Landparcel) {
            getArea() >> 30.0
        }
        Season season = Mock(Season)
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest(
            landparcelId: 1,
            season: '',
            cropName: 'Wheat',
            area: 20.0,
            description: ''
        )

        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >>
            [Mock(AgriculturalRecord) {
                getArea() >> 10.0
            }]

        when:
        agriculturalRecordService.validateCropArea(landparcel, season, request)

        then:
        noExceptionThrown()
    }

    def "should validate successfully if total existing area plus requested area is within limits"() {
        given:
        Landparcel landparcel = Mock(Landparcel) {
            getArea() >> 50.0
        }
        Season season = Mock(Season)
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest(
            landparcelId: 1,
            season: '',
            cropName: 'Wheat',
            area: 15.0,
            description: ''
        )

        AgriculturalRecord existingRecord1 = Mock(AgriculturalRecord) {
            getArea() >> 10.0
        }
        AgriculturalRecord existingRecord2 = Mock(AgriculturalRecord) {
            getArea() >> 20.0
        }

        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >>
            [existingRecord1, existingRecord2]

        when:
        agriculturalRecordService.validateCropArea(landparcel, season, request)

        then:
        noExceptionThrown()
    }

    /*
    * findAgriculturalRecordById
    */

    def "should find agricultural record by id"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, farm.getId())
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId
        }

        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.of(agriculturalRecord)

        when:
        AgriculturalRecord foundRecord = agriculturalRecordService.findAgriculturalRecordById(agriculturalRecordId.getId(),farm.getId())

        then:
        foundRecord == agriculturalRecord
    }

    def "should throw exception for non existing record by id"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, farm.getId())

        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.empty()

        when:
        agriculturalRecordService.findAgriculturalRecordById(agriculturalRecordId.getId(),farm.getId())

        then:
        RuntimeException exception = thrown(RuntimeException)
        exception.message == 'Nie znaleziono ewidencji'
    }

    /*
    * updateAgriculturalRecord
    */

    def "should update agricultural record successfully"() {
        given:
        Landparcel landparcel = Mock(Landparcel) {
            getArea() >> 20.0
        }
        Season season = Mock(Season)
        Crop crop = Mock(Crop)
        Farm farm = Mock(Farm) {
            getId() >> 1
        }

        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, 1)
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId
            getLandparcel() >> landparcel
            getSeason() >> season
            getCrop() >> Mock(Crop) {
                getName() >> 'Potato'
            }
        }
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest(
            cropName: 'Corn',
            area: 15.0,
            description: ''
        )

        userService.getLoggedUserFarm() >> farm
        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.of(agriculturalRecord)
        agriculturalRecordRepository.findByLandparcelAndSeasonAndCrop(landparcel, season, crop) >> []

        //Vaildatation functions mocks
        cropRepository.findByName(request.getCropName()) >> crop
        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >> []

        when:
        agriculturalRecordService.updateAgriculturalRecord(agriculturalRecordId.getId(), request)

        then:
        1 * agriculturalRecord.setCrop(crop)
        1 * agriculturalRecord.setArea(request.area)
        1 * agriculturalRecord.setDescription(request.description)
        1 * agriculturalRecordRepository.save(_ as AgriculturalRecord)
    }

    def "should throw exception if agricultural record is not found"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        userService.getLoggedUserFarm() >> farm

        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, 1)
        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.empty()

        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest(
            cropName: 'Corn',
            area: 15.0,
            description: ''
        )

        when:
        agriculturalRecordService.updateAgriculturalRecord(agriculturalRecordId.getId(), request)

        then:
        Exception exception = thrown(Exception)
        exception.message == 'Nie znaleziono ewidencji'
    }

    def "should throw exception if the new crop already exists on the landparcel for the season"() {
        given:
        Landparcel landparcel = Mock(Landparcel)
        Season season = Mock(Season)
        Crop crop = Mock(Crop)
        Farm farm = Mock(Farm) {
            getId() >> 1
        }

        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, 1)
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId
            getLandparcel() >> landparcel
            getSeason() >> season
            getCrop() >> Mock(Crop) {
                getName() >> 'Potato'
            }
        }
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest(
            cropName: 'Corn',
            area: 15.0,
            description: ''
        )

        userService.getLoggedUserFarm() >> farm
        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.of(agriculturalRecord)
        cropRepository.findByName(request.getCropName()) >> crop
        agriculturalRecordRepository.findByLandparcelAndSeasonAndCrop(landparcel, season, crop) >> [Mock(AgriculturalRecord)]

        when:
        agriculturalRecordService.updateAgriculturalRecord(agriculturalRecordId.getId(), request)

        then:
        Exception exception = thrown(Exception)
        exception.message == 'Wybrana uprawa już istnieje na tym polu'
    }

    def "should skip updating crop if name is same of current and update request"() {
        given:
        Landparcel landparcel = Mock(Landparcel) {
            getArea() >> 20.0
        }
        Season season = Mock(Season)
        Crop crop = Mock(Crop)
        Farm farm = Mock(Farm) {
            getId() >> 1
        }

        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, 1)
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId
            getLandparcel() >> landparcel
            getSeason() >> season
            getCrop() >> Mock(Crop) {
                getName() >> 'Corn'
            }
        }
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest(
            cropName: 'Corn',
            area: 15.0,
            description: ''
        )

        userService.getLoggedUserFarm() >> farm
        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.of(agriculturalRecord)
        agriculturalRecordRepository.findByLandparcelAndSeasonAndCrop(landparcel, season, crop) >> []

        // //Vaildatation functions mocks
        // cropRepository.findByName(request.getCropName()) >> crop
        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >> []

        when:
        agriculturalRecordService.updateAgriculturalRecord(agriculturalRecordId.getId(), request)

        then:
        0 * agriculturalRecord.setCrop(crop)
        1 * agriculturalRecord.setArea(request.area)
        1 * agriculturalRecord.setDescription(request.description)
        1 * agriculturalRecordRepository.save(_ as AgriculturalRecord)
    }
    /*
    * validateUpdatedCropArea
    */

    def "should validate updated crop area"() {
        given:
        Landparcel landparcel = Mock(Landparcel) {
            getArea() >> 15.0
        }
        Season season = Mock(Season)
        Crop crop = Mock(Crop)
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, 1)
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId
            getLandparcel() >> landparcel
            getSeason() >> season
            getCropName() >> crop
            getArea() >> 10.0
        }
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest(
            cropName: 'Corn',
            area: 15.0,
            description: ''
        )

        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >> []

        when:
        agriculturalRecordService.validateUpdatedCropArea(landparcel, season, request, agriculturalRecord)

        then:
        noExceptionThrown()
    }

    def "should throw exception if updated area exceeds available space"() {
        given:
        Landparcel landparcel = Mock(Landparcel) {
            getArea() >> 20.0
        }
        Season season = Mock(Season)
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, 1)
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId
            getLandparcel() >> landparcel
            getSeason() >> season
            getArea() >> 10.0
        }
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest(
            cropName: 'Corn',
            area: 15.0,
            description: ''
        )

        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >> [
            Mock(AgriculturalRecord) {
                getId() >> new AgriculturalRecordId(2, 1)
                getArea() >> 6.0
            }
        ]

        when:
        agriculturalRecordService.validateUpdatedCropArea(landparcel, season, request, agriculturalRecord)

        then:
        Exception exception = thrown(Exception)
        exception.message == 'Maksymalna niewykorzystana powierzchnia na tym polu to: 14.0 ha. Spróbuj najpierw zmniejszyć powierzchnię pozostałych upraw.'
    }

    def "should ignore area of record being updated"() {
        given:
        Landparcel landparcel = Mock(Landparcel) {
            getArea() >> 25.0
        }
        Season season = Mock(Season)
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, 1)
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId
            getLandparcel() >> landparcel
            getSeason() >> season
            getArea() >> 5.0
        }
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest(
            cropName: 'Corn',
            area: 15.0,
            description: ''
        )

        agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season) >> [
            Mock(AgriculturalRecord) {
                getId() >> new AgriculturalRecordId(1, 1) 
                getArea() >> 5.0
            },
            Mock(AgriculturalRecord) {
                getId() >> new AgriculturalRecordId(2, 1)
                getArea() >> 10.0
            }
        ]

        when:
        agriculturalRecordService.validateUpdatedCropArea(landparcel, season, request, agriculturalRecord)

        then:
        noExceptionThrown()
    }

    /*
    * createAgriculturalRecordForLandparcel
    */

    def "should create agricultural record for landparcel"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Landparcel landparcel = Mock(Landparcel)
        Season season = Mock(Season)
        String cropName = 'uprawa nieoznaczona'
        Crop crop = Mock(Crop) {
            getName() >> cropName
        }

        cropRepository.findByName(cropName) >> crop
        agriculturalRecordRepository.findNextFreeIdForFarm(farm.getId()) >> 2

        when:
        agriculturalRecordService.createAgriculturalRecordForLandparcel(landparcel, farm, season)

        then:
        1 * agriculturalRecordRepository.save(_ as AgriculturalRecord)
    }

    /*
    * deleteAgriculturalRecord
    */

    def "should delete agricultural record"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        AgroActivity agroActivity1 = Mock(AgroActivity) {
            getId() >> Mock(AgroActivityId) {
                getId() >> 1
                getFarmId() >> 1
            }
        }
        AgroActivity agroActivity2 = Mock(AgroActivity) {
            getId() >> Mock(AgroActivityId) {
                getId() >> 2
                getFarmId() >> 1
            }
        }
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, 1)
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId
            getFarm() >> farm
        }

        userService.getLoggedUserFarm() >> farm
        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.of(agriculturalRecord)
        agriculturalRecordRepository.existsById(agriculturalRecordId) >> true
        agroActivityRepository.findByAgriculturalRecordId(agriculturalRecordId) >> [agroActivity1, agroActivity2]

        when:
        agriculturalRecordService.deleteAgriculturalRecord(agriculturalRecordId.getId())

        then:
        1 * agroActivityService.deleteAgroActivity(agroActivity1.getId())
        1 * agroActivityService.deleteAgroActivity(_)
        1 * agriculturalRecordRepository.deleteById(agriculturalRecordId)
    }

    def "should throw exception if agricultural record is not found"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(1, 1)

        userService.getLoggedUserFarm() >> farm
        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.empty()
        agriculturalRecordRepository.existsById(agriculturalRecordId) >> false

        when:
        agriculturalRecordService.deleteAgriculturalRecord(agriculturalRecordId.getId())

        then:
        Exception exception = thrown(Exception)
        exception.message == 'Ewidencja, którą próbujesz usunąć nie istnieje!'
    }

    /*
    * deleteAllAgriculturalRecordsForFarm
    */

    def "should delete all agricultural records for farm"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        AgriculturalRecordId agriculturalRecordId1 = new AgriculturalRecordId(1, 1)
        AgriculturalRecordId agriculturalRecordId2 = new AgriculturalRecordId(2, 1)
        AgriculturalRecord agriculturalRecord1 = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId1
            getFarm() >> farm
        }
        AgriculturalRecord agriculturalRecord2 = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId2
            getFarm() >> farm
        }
        AgroActivity agroActivity1 = Mock(AgroActivity) {
            getId() >> Mock(AgroActivityId) {
                getId() >> 1
                getFarmId() >> 1
            }
            getAgriculturalRecord() >> agriculturalRecord1
        }
        AgroActivity agroActivity2 = Mock(AgroActivity) {
            getId() >> Mock(AgroActivityId) {
                getId() >> 2
                getFarmId() >> 1
            }
            getAgriculturalRecord() >> agriculturalRecord1
        }
        AgroActivity agroActivity3 = Mock(AgroActivity) {
            getId() >> Mock(AgroActivityId) {
                getId() >> 2
                getFarmId() >> 1
            }
            getAgriculturalRecord() >> agriculturalRecord2
        }

        userService.getLoggedUserFarm() >> farm
        agriculturalRecordRepository.findAgriculturalRecordByFarm(farm) >> [agriculturalRecord1, agriculturalRecord2]
        agriculturalRecordRepository.existsById(agriculturalRecordId1) >> true
        agriculturalRecordRepository.existsById(agriculturalRecordId2) >> true
        agroActivityRepository.findByAgriculturalRecordId(agriculturalRecordId1) >> [agroActivity1, agroActivity2]
        agroActivityRepository.findByAgriculturalRecordId(agriculturalRecordId2) >> [agroActivity3]

        when:
        agriculturalRecordService.deleteAllAgriculturalRecordsForFarm(farm)

        then:
        1 * agroActivityService.deleteAgroActivity(agroActivity1.getId())
        1 * agroActivityService.deleteAgroActivity(agroActivity2.getId())
        1 * agriculturalRecordRepository.deleteById(agriculturalRecordId1)

        1 * agroActivityService.deleteAgroActivity(agroActivity3.getId())
        1 * agriculturalRecordRepository.deleteById(agriculturalRecordId2)
    }

}
