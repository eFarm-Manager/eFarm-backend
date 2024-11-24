package com.efarm.efarmbackend.service.landparcel

import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus
import com.efarm.efarmbackend.model.landparcel.LandOwnershipStatus
import com.efarm.efarmbackend.model.landparcel.Landparcel
import com.efarm.efarmbackend.model.landparcel.LandparcelId
import com.efarm.efarmbackend.model.landparcel.LandparcelDTO
import com.efarm.efarmbackend.repository.landparcel.LandOwnershipStatusRepository
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository
import spock.lang.Specification
import spock.lang.Subject

class LandparcelServiceSpec extends Specification {

    def landOwnershipStatusRepository = Mock(LandOwnershipStatusRepository)
    def landparcelRepository = Mock(LandparcelRepository)

    @Subject
    LandparcelService landparcelService = new LandparcelService(
            landOwnershipStatusRepository,
            landparcelRepository
    )
    /*
    * addNewLandparcelData
    */

    def "should set ownership status and call setters when valid status provided"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO) {
            getLandOwnershipStatus() >> 'STATUS_PRIVATELY_OWNED'
            getVoivodeship() >> 'Mazowieckie'
            getDistrict() >> 'Warszawa'
            getCommune() >> 'Mokotów'
            getGeodesyDistrictNumber() >> 'XYZ123'
            getLandparcelNumber() >> 'LP-001'
            getLongitude() >> 21.0122
            getLatitude() >> 52.2297
            getArea() >> 1500.0
        }
        Landparcel landparcel = new Landparcel()
        LandOwnershipStatus ownershipStatus = new LandOwnershipStatus()
        ownershipStatus.setOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)

        landOwnershipStatusRepository.findByOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED) >> Optional.of(ownershipStatus)

        when:
        landparcelService.addNewLandparcelData(landparcelDTO, landparcel)

        then:
        landparcel.getLandOwnershipStatus().getOwnershipStatus() == ownershipStatus.getOwnershipStatus()
        landparcel.getVoivodeship() == landparcelDTO.getVoivodeship()
        landparcel.getDistrict() == landparcelDTO.getDistrict()
        landparcel.getCommune() == landparcelDTO.getCommune()
        landparcel.getGeodesyDistrictNumber() == landparcelDTO.getGeodesyDistrictNumber()
        landparcel.getLandparcelNumber() == landparcelDTO.getLandparcelNumber()
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == landparcelDTO.getLatitude()
        landparcel.getArea() == landparcelDTO.getArea()
    }

    def "should set ownership status to STATUS_LEASE when provided"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO) {
            getLandOwnershipStatus() >> 'STATUS_LEASE'
            getVoivodeship() >> 'Mazowieckie'
            getDistrict() >> 'Warszawa'
            getCommune() >> 'Mokotów'
            getGeodesyDistrictNumber() >> 'XYZ123'
            getLandparcelNumber() >> 'LP-001'
            getLongitude() >> 21.0122
            getLatitude() >> 52.2297
            getArea() >> 1500.0
        }
        Landparcel landparcel = new Landparcel()
        LandOwnershipStatus ownershipStatus = new LandOwnershipStatus()
        ownershipStatus.setOwnershipStatus(ELandOwnershipStatus.STATUS_LEASE)

        landOwnershipStatusRepository.findByOwnershipStatus(ELandOwnershipStatus.STATUS_LEASE) >> Optional.of(ownershipStatus)

        when:
        landparcelService.addNewLandparcelData(landparcelDTO, landparcel)

        then:
        landparcel.getLandOwnershipStatus().getOwnershipStatus() == ELandOwnershipStatus.STATUS_LEASE
        landparcel.getVoivodeship() == landparcelDTO.getVoivodeship()
        landparcel.getDistrict() == landparcelDTO.getDistrict()
        landparcel.getCommune() == landparcelDTO.getCommune()
        landparcel.getGeodesyDistrictNumber() == landparcelDTO.getGeodesyDistrictNumber()
        landparcel.getLandparcelNumber() == landparcelDTO.getLandparcelNumber()
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == landparcelDTO.getLatitude()
        landparcel.getArea() == landparcelDTO.getArea()
    }

    def "should set ownership status to STATUS_LEASE when invalid provided"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO) {
            getLandOwnershipStatus() >> 'INVALID'
            getVoivodeship() >> 'Mazowieckie'
            getDistrict() >> 'Warszawa'
            getCommune() >> 'Mokotów'
            getGeodesyDistrictNumber() >> 'XYZ123'
            getLandparcelNumber() >> 'LP-001'
            getLongitude() >> 21.0122
            getLatitude() >> 52.2297
            getArea() >> 1500.0
        }
        Landparcel landparcel = new Landparcel()
        LandOwnershipStatus ownershipStatus = new LandOwnershipStatus()
        ownershipStatus.setOwnershipStatus(ELandOwnershipStatus.STATUS_LEASE)

        landOwnershipStatusRepository.findByOwnershipStatus(ELandOwnershipStatus.STATUS_LEASE) >> Optional.of(ownershipStatus)

        when:
        landparcelService.addNewLandparcelData(landparcelDTO, landparcel)

        then:
        landparcel.getLandOwnershipStatus().getOwnershipStatus() == ELandOwnershipStatus.STATUS_LEASE
        landparcel.getVoivodeship() == landparcelDTO.getVoivodeship()
        landparcel.getDistrict() == landparcelDTO.getDistrict()
        landparcel.getCommune() == landparcelDTO.getCommune()
        landparcel.getGeodesyDistrictNumber() == landparcelDTO.getGeodesyDistrictNumber()
        landparcel.getLandparcelNumber() == landparcelDTO.getLandparcelNumber()
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == landparcelDTO.getLatitude()
        landparcel.getArea() == landparcelDTO.getArea()
    }
    /*
    * updateLandparcelData
    */

    def "should update common fields when valid data is provided"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO) {
            getLongitude() >> 21.0122
            getLatitude() >> 52.2297
            getArea() >> 1500.0
        }
        Landparcel landparcel = new Landparcel()
        landparcel.setArea(750)
        landparcel.setLongitude(22)
        landparcel.setLatitude(53)
        LandOwnershipStatus currentOwnershipStatus = new LandOwnershipStatus()
        currentOwnershipStatus.setOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
        landparcel.setLandOwnershipStatus(currentOwnershipStatus)

        landOwnershipStatusRepository.findByOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED) >> Optional.of(currentOwnershipStatus)

        when:
        landparcelService.updateLandparcelData(landparcelDTO, landparcel)

        then:
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == landparcelDTO.getLatitude()
        landparcel.getArea() == landparcelDTO.getArea()
    }

    def "should update ownership status when different valid status is provided"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO) {
            getLandOwnershipStatus() >> 'STATUS_LEASE'
        }
        Landparcel landparcel = new Landparcel()
        LandOwnershipStatus currentOwnershipStatus = new LandOwnershipStatus()
        currentOwnershipStatus.setOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
        landparcel.setLandOwnershipStatus(currentOwnershipStatus)

        LandOwnershipStatus newOwnershipStatus = new LandOwnershipStatus()
        newOwnershipStatus.setOwnershipStatus(ELandOwnershipStatus.STATUS_LEASE)

        landOwnershipStatusRepository.findByOwnershipStatus(ELandOwnershipStatus.STATUS_LEASE) >> Optional.of(newOwnershipStatus)

        when:
        landparcelService.updateLandparcelData(landparcelDTO, landparcel)

        then:
        landparcel.getLandOwnershipStatus().getOwnershipStatus() == ELandOwnershipStatus.STATUS_LEASE
    }

    def "should handle invalid ownership status gracefully"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO) {
            getLandOwnershipStatus() >> 'INVALID_STATUS'
            getLongitude() >> 21.0122
            getLatitude() >> 52.2297
            getArea() >> 1500.0
        }
        Landparcel landparcel = new Landparcel()
        LandOwnershipStatus currentOwnershipStatus = new LandOwnershipStatus()
        currentOwnershipStatus.setOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
        landparcel.setLandOwnershipStatus(currentOwnershipStatus)

        when:
        landparcelService.updateLandparcelData(landparcelDTO, landparcel)

        then:
        landparcel.getLandOwnershipStatus().getOwnershipStatus() == ELandOwnershipStatus.STATUS_PRIVATELY_OWNED
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == landparcelDTO.getLatitude()
        landparcel.getArea() == landparcelDTO.getArea()
    }

    /*
    * isLandparcelAlreadyExistingByFarm
    */

    def "should return true when land parcel already exists"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO){
            getDistrict() >> 'District'
            getCommune() >> 'Commune'
            getGeodesyDistrictNumber() >> '987654'
            getLandparcelNumber() >> '12345'
            getGeodesyLandparcelNumber() >> '25312.05'
        }

        Farm farm = Mock(Farm)
        landparcelRepository.existsByGeodesyLandparcelNumberAndFarm(
            landparcelDTO.getGeodesyLandparcelNumber(),
            farm
        ) >> true

        when:
        Boolean result = landparcelService.isLandparcelAlreadyExistingByFarm(landparcelDTO, farm)

        then:
        result == true
    }

    def "should return false when land parcel does not exist"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO){
            getDistrict() >> 'District'
            getCommune() >> 'Commune'
            getGeodesyDistrictNumber() >> '987654'
            getLandparcelNumber() >> '12345'
            getGeodesyLandparcelNumber() >> '25312.05'
        }
        Farm farm = new Farm()

        landparcelRepository.existsByGeodesyLandparcelNumberAndFarm(landparcelDTO.getGeodesyLandparcelNumber(), farm) >> false

        when:
        Boolean result = landparcelService.isLandparcelAlreadyExistingByFarm(landparcelDTO, farm)

        then:
        result == false
    }
    /*
    * isLandparcelNameTaken
    */

    def "should return true when land parcel name is taken"() {
        given:
        Farm farm = Mock(Farm)
        String name = '987654'
        landparcelRepository.existsByFarmAndName(farm, name) >> true

        when:
        Boolean result = landparcelService.isLandparcelNameTaken(name, farm)

        then:
        result == true
    }

    def "should return false when land parcel name is not taken"() {
        given:
        Farm farm = Mock(Farm)
        String name = '987654'
        landparcelRepository.existsByFarmAndName(farm, name) >> false

        when:
        Boolean result = landparcelService.isLandparcelNameTaken(name, farm)

        then:
        result == false
    }

    /*
    * findlandparcelByFarm
    */

    def "should find landparcel by current farm"() {
        given:
        Integer id = 1
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        LandparcelId landparcelId = new LandparcelId(id, farm.getId())
        Landparcel landparcel = Mock(Landparcel) {
            getId() >> landparcelId
        }
        landparcelRepository.findById(landparcelId) >> Optional.of(landparcel) 

        when:
        Landparcel result = landparcelService.findlandparcelByFarm(id, farm)

        then:
        result.getId().getId() == landparcelId.getId()
        result.getId().getFarmId() == landparcelId.getFarmId()
    }

    def "should return null when landparcel does not exist"() {
        given:
        Integer id = 1
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        LandparcelId landparcelId = new LandparcelId(id, farm.getId())
        landparcelRepository.findById(landparcelId) >> Optional.empty()

        when:
        Landparcel result = landparcelService.findlandparcelByFarm(id, farm)

        then:
        Exception e = thrown()
        e.message == 'Nie znaleziono działki'
    }

    /*
    * deleteAllLandparcelsForFarm
    */

    def "should delete all landparcels for a farm"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Landparcel landparcel1 = Mock(Landparcel)
        Landparcel landparcel2 = Mock(Landparcel)
        landparcelRepository.findByFarmId(farm.getId()) >> [landparcel1, landparcel2]

        when:
        landparcelService.deleteAllLandparcelsForFarm(farm)

        then:
        1 * landparcelRepository.deleteAll({ List<Landparcel> landparcels ->
            landparcels.contains(landparcel1) && landparcels.contains(landparcel2)
        })
    }

    /*
    * setCommonFields
    */

    def "should set common fields when valid values are provided"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO){
            getName() >> 'Landparcel'
            getLongitude() >> 21.0122
            getLatitude() >> null
            getArea() >> 1500.0
        }
        Landparcel landparcel = new Landparcel()

        when:
        landparcelService.setCommonFields(landparcel, landparcelDTO)

        then:
        landparcel.getName() == landparcelDTO.getName()
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == null
        landparcel.getArea() == landparcelDTO.getArea()
    }
    /*
    * setAdministrativeData
    */

    def "should set all administrative data when all fields are provided"() {
        given:
        LandparcelDTO landparcelDTO = Mock(LandparcelDTO){
            getVoivodeship() >> 'Lubelskie'
            getDistrict() >> 'district'
            getCommune() >> null
            getGeodesyDistrictNumber() >> '987654'
            getLandparcelNumber() >> '12345'
            getGeodesyLandparcelNumber() >> '25312.05'
        }
        Landparcel landparcel = new Landparcel()

        when:
        landparcelService.setAdministrativeData(landparcel, landparcelDTO)

        then:
        landparcel.getVoivodeship() == landparcelDTO.getVoivodeship()
        landparcel.getDistrict() == landparcelDTO.getDistrict()
        landparcel.getCommune() == null
        landparcel.getGeodesyDistrictNumber() == landparcelDTO.getGeodesyDistrictNumber()
        landparcel.getLandparcelNumber() == landparcelDTO.getLandparcelNumber()
        landparcel.getGeodesyLandparcelNumber() == landparcelDTO.getGeodesyLandparcelNumber()
    }

}
