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
            landOwnershipStatusRepository: landOwnershipStatusRepository,
            landparcelRepository: landparcelRepository
    )

    def "should set ownership status and call setters when valid status provided"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            landOwnershipStatus: 'STATUS_PRIVATELY_OWNED',
            voivodeship: 'Mazowieckie',
            district: 'Warszawa',
            commune: 'Mokotów',
            geodesyRegistrationDistrictNumber: 'XYZ123',
            landparcelNumber: 'LP-001',
            longitude: 21.0122,
            latitude: 52.2297,
            area: 1500.0
        )
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
        landparcel.getGeodesyRegistrationDistrictNumber() == landparcelDTO.getGeodesyRegistrationDistrictNumber()
        landparcel.getLandparcelNumber() == landparcelDTO.getLandparcelNumber()
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == landparcelDTO.getLatitude()
        landparcel.getArea() == landparcelDTO.getArea()
    }

    def "should set ownership status to STATUS_LEASE when provided"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            landOwnershipStatus: 'STATUS_LEASE',
            voivodeship: 'Mazowieckie',
            district: 'Warszawa',
            commune: 'Mokotów',
            geodesyRegistrationDistrictNumber: 'XYZ123',
            landparcelNumber: 'LP-001',
            longitude: 21.0122,
            latitude: 52.2297,
            area: 1500.0
        )
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
        landparcel.getGeodesyRegistrationDistrictNumber() == landparcelDTO.getGeodesyRegistrationDistrictNumber()
        landparcel.getLandparcelNumber() == landparcelDTO.getLandparcelNumber()
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == landparcelDTO.getLatitude()
        landparcel.getArea() == landparcelDTO.getArea()
    }

    def "should set ownership status to STATUS_LEASE when invalid provided"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            landOwnershipStatus: 'INVALID',
            voivodeship: 'Mazowieckie',
            district: 'Warszawa',
            commune: 'Mokotów',
            geodesyRegistrationDistrictNumber: 'XYZ123',
            landparcelNumber: 'LP-001',
            longitude: 21.0122,
            latitude: 52.2297,
            area: 1500.0
        )
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
        landparcel.getGeodesyRegistrationDistrictNumber() == landparcelDTO.getGeodesyRegistrationDistrictNumber()
        landparcel.getLandparcelNumber() == landparcelDTO.getLandparcelNumber()
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == landparcelDTO.getLatitude()
        landparcel.getArea() == landparcelDTO.getArea()
    }

    def "should update common fields when valid data is provided"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            longitude: 21.0122,
            latitude: 52.2297,
            area: 1500.0
        )
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
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            landOwnershipStatus: 'STATUS_LEASE'
        )
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
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            landOwnershipStatus: 'INVALID_STATUS',
            longitude: 21.0122,
            latitude: 52.2297,
            area: 1500.0
        )
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

    def "should correctly create DTO from Landparcel"() {
        given:
        LandparcelId landparcelId = new LandparcelId(1, 1)
        Landparcel landparcel = new Landparcel()
        landparcel.setId(landparcelId)
        landparcel.setLandOwnershipStatus(new LandOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED))
        landparcel.setVoivodeship('Mazowieckie')
        landparcel.setDistrict('Warszawa')
        landparcel.setCommune('Mokotów')
        landparcel.setGeodesyRegistrationDistrictNumber('XYZ123')
        landparcel.setLandparcelNumber('LP-001')
        landparcel.setLongitude(21.0122)
        landparcel.setLatitude(52.2297)
        landparcel.setArea(1500.0)

        when:
        LandparcelDTO landparcelDTO = landparcelService.createDTOtoDisplay(landparcel)

        then:
        landparcelDTO.getId() == landparcel.getId().getId()
        landparcelDTO.getLandOwnershipStatus() == landparcel.getLandOwnershipStatus().getOwnershipStatus().toString()
        landparcelDTO.getVoivodeship() == landparcel.getVoivodeship()
        landparcelDTO.getDistrict() == landparcel.getDistrict()
        landparcelDTO.getCommune() == landparcel.getCommune()
        landparcelDTO.getGeodesyRegistrationDistrictNumber() == landparcel.getGeodesyRegistrationDistrictNumber()
        landparcelDTO.getLandparcelNumber() == landparcel.getLandparcelNumber()
        landparcelDTO.getLongitude() == landparcel.getLongitude()
        landparcelDTO.getLatitude() == landparcel.getLatitude()
        landparcelDTO.getArea() == landparcel.getArea()
    }

    def "should return true when land parcel already exists"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            district: 'District',
            commune: 'Commune',
            geodesyRegistrationDistrictNumber: '987654',
            landparcelNumber: '12345'
        )

        Farm farm = Mock(Farm)
        landparcelRepository.existsByDistrictAndCommuneAndGeodesyRegistrationDistrictNumberAndLandparcelNumberAndFarm(
            landparcelDTO.getDistrict(),
            landparcelDTO.getCommune(),
            landparcelDTO.getGeodesyRegistrationDistrictNumber(),
            landparcelDTO.getLandparcelNumber(),
            farm
        ) >> true

        when:
        Boolean result = landparcelService.isLandparcelAlreadyExistingByFarm(landparcelDTO, farm)

        then:
        result == true
    }

    def "should return false when land parcel does not exist"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            district: 'District',
            commune: 'Commune',
            geodesyRegistrationDistrictNumber: '987654',
            landparcelNumber: '12345'
        )
        Farm farm = new Farm()

        landparcelRepository.existsByDistrictAndCommuneAndGeodesyRegistrationDistrictNumberAndLandparcelNumberAndFarm(
            landparcelDTO.getDistrict(),
            landparcelDTO.getCommune(),
            landparcelDTO.getGeodesyRegistrationDistrictNumber(),
            landparcelDTO.getLandparcelNumber(),
            farm
        ) >> false

        when:
        Boolean result = landparcelService.isLandparcelAlreadyExistingByFarm(landparcelDTO, farm)

        then:
        result == false
    }

    def "should set common fields when valid values are provided"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            longitude: 21.0122,
            latitude: null,
            area: 1500.0
        )
        Landparcel landparcel = new Landparcel()

        when:
        landparcelService.setCommonFields(landparcel, landparcelDTO)

        then:
        landparcel.getLongitude() == landparcelDTO.getLongitude()
        landparcel.getLatitude() == null
        landparcel.getArea() == landparcelDTO.getArea()
    }

    def "should set all administrative data when all fields are provided"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            voivodeship: 'Lubelskie',
            district: 'district',
            commune: null,
            geodesyRegistrationDistrictNumber: '987654',
            landparcelNumber: '12345'
        )
        Landparcel landparcel = new Landparcel()

        when:
        landparcelService.setAdministrativeData(landparcel, landparcelDTO)

        then:
        landparcel.getVoivodeship() == landparcelDTO.getVoivodeship()
        landparcel.getDistrict() == landparcelDTO.getDistrict()
        landparcel.getCommune() == null
        landparcel.getGeodesyRegistrationDistrictNumber() == landparcelDTO.getGeodesyRegistrationDistrictNumber()
        landparcel.getLandparcelNumber() == landparcelDTO.getLandparcelNumber()
    }

}
