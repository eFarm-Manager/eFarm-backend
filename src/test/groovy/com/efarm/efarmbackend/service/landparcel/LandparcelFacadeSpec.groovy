package com.efarm.efarmbackend.service.landparcel;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import com.efarm.efarmbackend.model.landparcel.LandparcelId;
import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus
import com.efarm.efarmbackend.model.landparcel.LandOwnershipStatus
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spock.lang.Specification
import spock.lang.Subject
import java.time.LocalDate;
import java.util.List;

class LandparcelFacadeSpec extends Specification {

    def landparcelService = Mock(LandparcelService)
    def landparcelRepository = Mock(LandparcelRepository)
    def userService = Mock(UserService)

    @Subject
    LandparcelFacade landparcelFacade = new LandparcelFacade(
            landparcelService: landparcelService,
            landparcelRepository: landparcelRepository,
            userService: userService
    )
    /*
        addNewLandparcel
    */

    def "should add a new land parcel when it does not already exist"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            landOwnershipStatus: "STATUS_PRIVATELY_OWNED",
            voivodeship: "Lubelskie",
            district: "district",
            commune: "commune",
            geodesyRegistrationDistrictNumber: "XYZ123",
            landparcelNumber: "LP-001",
            longitude: 21.0122,
            latitude: 52.2297,
            area: 1500.0
        )

        Farm farm = Mock(Farm)
        Landparcel landparcel = Mock(Landparcel)
        LandparcelId landparcelId = Mock(LandparcelId)

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findNextFreeIdForFarm(farm.getId()) >> 123
        landparcelId = new LandparcelId(123, farm.getId())

        landparcelService.isLandparcelAlreadyExsists(landparcelDTO, farm) >> false

        when:
        landparcelFacade.addNewLandparcel(landparcelDTO)

        then:
        1 * landparcelService.addNewLandparcelData(_, _)
        1 * landparcelRepository.save(_)
    }

    def "should throw exception when land parcel already exists"() {
        given:
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            landOwnershipStatus: "STATUS_PRIVATELY_OWNED",
            voivodeship: "Lubelskie",
            district: "district",
            commune: "commune",
            geodesyRegistrationDistrictNumber: "XYZ123",
            landparcelNumber: "LP-001",
            longitude: 21.0122,
            latitude: 52.2297,
            area: 1500.0
        )

        Farm farm = Mock(Farm)

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findNextFreeIdForFarm(farm.getId()) >> 123

        landparcelService.isLandparcelAlreadyExsists(landparcelDTO, farm) >> true

        when:
        landparcelFacade.addNewLandparcel(landparcelDTO)

        then:
        Exception e = thrown(Exception)
        e.message == "Działka o powyższych danych geodezyjnych już istnieje!"

        0 * landparcelService.addNewLandparcelData(_, _)
        0 * landparcelRepository.save(_)
    }
    /*
        getLandparcelDetails
    */

    def "should return landparcel details when landparcel exists and is available"() {
        given:
        Integer id = 1
        Farm farm = Mock(Farm)
        Landparcel landparcel = new Landparcel()
        landparcel.setArea(522)
        landparcel.setIsAvailable(true)

        LandparcelId landparcelId = new LandparcelId(id, farm.getId())
        landparcel.setId(landparcelId)

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findById(landparcelId) >> Optional.of(landparcel)
        LandparcelDTO landparcelDTO = new LandparcelDTO()
        landparcelDTO.setId(id)
        landparcelDTO.setArea(landparcel.getArea())

        landparcelService.createDTOtoDisplay(landparcel) >> landparcelDTO

        when:
        LandparcelDTO result = landparcelFacade.getLandparcelDetails(id)

        then:
        result instanceof LandparcelDTO
        result.getId() == landparcel.getId().getId()
        result.getArea() == landparcel.getArea()
    }

    def "should throw exception when landparcel does not exist"() {
        given:
        Integer id = 1
        Farm farm = Mock(Farm)
        LandparcelId landparcelId = new LandparcelId(id, farm.getId())

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findById(landparcelId) >> Optional.empty()

        when:
        landparcelFacade.getLandparcelDetails(id)

        then:
        Exception ex = thrown()
        ex.message == "Działka o id: 1 nie została znaleziona"
    }

    def "should throw exception when landparcel is not available"() {
        given:
        Integer id = 1
        Farm farm = Mock(Farm)
        Landparcel landparcel = new Landparcel()
        landparcel.setIsAvailable(false)

        LandparcelId landparcelId = new LandparcelId(id, farm.getId())

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findById(landparcelId) >> Optional.of(landparcel)

        when:
        landparcelFacade.getLandparcelDetails(id)

        then:
        Exception ex = thrown()
        ex.message == "Wybrana działka już nie istnieje"
    }
    /*
        updateLandparcel
    */

    def "should update landparcel when it exists and is available"() {
        given:
        Integer id = 1
        Farm farm = Mock(Farm)
        LandparcelDTO landparcelDTO = new LandparcelDTO(
            longitude: 21.0122,
            latitude: 52.2297,
            area: 1500.0
        )
        
        LandparcelId landparcelId = new LandparcelId(id, farm.getId())
        Landparcel landparcel = new Landparcel()
        landparcel.setId(landparcelId)
        landparcel.setIsAvailable(true)

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findById(landparcelId) >> Optional.of(landparcel)

        when:
        landparcelFacade.updateLandparcel(id, landparcelDTO)

        then:
        1 * landparcelService.updateLandparcelData(landparcelDTO, landparcel)
        1 * landparcelRepository.save(landparcel) 
    }

    def "should throw exception when landparcel does not exist"() {
        given:
        Integer id = 2
        Farm farm = Mock(Farm)
        
        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findById(_ as LandparcelId) >> Optional.empty()

        when:
        landparcelFacade.updateLandparcel(id, new LandparcelDTO())

        then:
        Exception ex = thrown(Exception)
        ex.message == "Działka nie istnieje"
    }

    def "should throw exception when landparcel is not available"() {
        given:
        Integer id = 3
        Farm farm = Mock(Farm)
        LandparcelDTO landparcelDTO = new LandparcelDTO()

        LandparcelId landparcelId = new LandparcelId(id, farm.getId())
        Landparcel landparcel = new Landparcel()
        landparcel.setId(landparcelId)
        landparcel.setIsAvailable(false)

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findById(landparcelId) >> Optional.of(landparcel)

        when:
        landparcelFacade.updateLandparcel(id, landparcelDTO)

        then:
        def exception = thrown(Exception)
        exception.message == "Wybrana działka już nie istnieje"
    }
    /*
        deleteLandparcel
    */

    def "should mark landparcel as unavailable when it exists and is available"() {
        given:
        Integer id = 1
        Farm farm = Mock(Farm)
        LandparcelId landparcelId = new LandparcelId(id, farm.getId())
        Landparcel landparcel = new Landparcel()
        landparcel.setId(landparcelId)
        landparcel.setIsAvailable(true)

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findById(landparcelId) >> Optional.of(landparcel)

        when:
        landparcelFacade.deleteLandparcel(id)

        then:
        1 * landparcelRepository.save(landparcel) 
        !landparcel.getIsAvailable() 
    }

    def "should throw exception when landparcel does not exist"() {
        given:
        Integer id = 2
        Farm farm = Mock(Farm)

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findById(_) >> Optional.empty() 

        when:
        landparcelFacade.deleteLandparcel(id)

        then:
        def ex = thrown(RuntimeException)
        ex.message == "Nie znaleziono działki o id: " + id
    }

    def "should throw exception when landparcel is not available"() {
        given:
        Integer id = 3
        Farm farm = Mock(Farm)
        LandparcelId landparcelId = new LandparcelId(id, farm.getId())
        Landparcel landparcel = new Landparcel()
        landparcel.setId(landparcelId)
        landparcel.setIsAvailable(false)

        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findById(landparcelId) >> Optional.of(landparcel)

        when:
        landparcelFacade.deleteLandparcel(id)

        then:
        def ex = thrown(Exception)
        ex.message == "Wybrana działka już nie istnieje!"
    }
    /*
        getLandparcels
    */

    def "should return all landparcels when no filters are applied"() {
        given:
        Farm farm = Mock(Farm)
        LandOwnershipStatus ownerStatus = new LandOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
        
        Landparcel landparcel1 = new Landparcel()
        landparcel1.setId(new LandparcelId(1, farm.getId()))
        landparcel1.setLandOwnershipStatus(ownerStatus)
        landparcel1.setArea(500.0)
        landparcel1.setCommune("CommuneA")
        landparcel1.setGeodesyRegistrationDistrictNumber("GRD1")
        landparcel1.setLandparcelNumber("LP1")
        
        Landparcel landparcel2 = new Landparcel()
        landparcel2.setId(new LandparcelId(2, farm.getId()))
        landparcel2.setLandOwnershipStatus(ownerStatus)
        landparcel2.setArea(600.0)
        landparcel2.setCommune("CommuneB")
        landparcel2.setGeodesyRegistrationDistrictNumber("GRD2")
        landparcel2.setLandparcelNumber("LP2")
    
        List<Landparcel> landparcelList = [landparcel1, landparcel2]
        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findByFarmId(farm.getId()) >> landparcelList
    
        when:
        List<LandparcelDTO> result = landparcelFacade.getLandparcels(null, null, null)
    
        then:
        result.size() == 2
        result[0].getArea() == 500.0
        result[1].getArea() == 600.0
    }

    def "should filter landparcels by search string"() {
        given:
        Farm farm = Mock(Farm)
        LandOwnershipStatus ownerStatus = new LandOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
        String searchString = "CommuneA"

        Landparcel landparcel1 = new Landparcel()
        landparcel1.setId(new LandparcelId(1, farm.getId()))
        landparcel1.setLandOwnershipStatus(ownerStatus)
        landparcel1.setArea(500.0)
        landparcel1.setCommune("CommuneA")
        landparcel1.setGeodesyRegistrationDistrictNumber("GRD1")
        landparcel1.setLandparcelNumber("LP1")

        Landparcel landparcel2 = new Landparcel()
        landparcel2.setId(new LandparcelId(2, farm.getId()))
        landparcel2.setLandOwnershipStatus(ownerStatus)
        landparcel2.setArea(600.0)
        landparcel2.setCommune("CommuneB")
        landparcel2.setGeodesyRegistrationDistrictNumber("GRD2")
        landparcel2.setLandparcelNumber("LP2")

        List<Landparcel> landparcelList = [landparcel1, landparcel2]
        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findByFarmId(farm.getId()) >> landparcelList

        when:
        List<LandparcelDTO> result = landparcelFacade.getLandparcels(searchString, null, null)

        then:
        result.size() == 1
        result[0].getCommune() == "CommuneA"
    }

    def "should filter landparcels by minimum area"() {
        given:
        Farm farm = Mock(Farm)
        LandOwnershipStatus ownerStatus = new LandOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
        Double minArea = 550.0

        Landparcel landparcel1 = new Landparcel()
        landparcel1.setId(new LandparcelId(1, farm.getId()))
        landparcel1.setLandOwnershipStatus(ownerStatus)
        landparcel1.setArea(500.0)
        landparcel1.setCommune("CommuneA")
        landparcel1.setGeodesyRegistrationDistrictNumber("GRD1")
        landparcel1.setLandparcelNumber("LP1")

        Landparcel landparcel2 = new Landparcel()
        landparcel2.setId(new LandparcelId(2, farm.getId()))
        landparcel2.setLandOwnershipStatus(ownerStatus)
        landparcel2.setArea(600.0)
        landparcel2.setCommune("CommuneB")
        landparcel2.setGeodesyRegistrationDistrictNumber("GRD2")
        landparcel2.setLandparcelNumber("LP2")

        List<Landparcel> landparcelList = [landparcel1, landparcel2]
        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findByFarmId(farm.getId()) >> landparcelList

        when:
        List<LandparcelDTO> result = landparcelFacade.getLandparcels(null, minArea, null)

        then:
        result.size() == 1
        result[0].getArea() == 600.0
    }

    def "should filter landparcels by maximum area"() {
        given:
        Farm farm = Mock(Farm)
        LandOwnershipStatus ownerStatus = new LandOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
        Double maxArea = 550.0

        Landparcel landparcel1 = new Landparcel()
        landparcel1.setId(new LandparcelId(1, farm.getId()))
        landparcel1.setLandOwnershipStatus(ownerStatus)
        landparcel1.setArea(500.0)
        landparcel1.setCommune("CommuneA")
        landparcel1.setGeodesyRegistrationDistrictNumber("GRD1")
        landparcel1.setLandparcelNumber("LP1")

        Landparcel landparcel2 = new Landparcel()
        landparcel2.setId(new LandparcelId(2, farm.getId()))
        landparcel2.setLandOwnershipStatus(ownerStatus)
        landparcel2.setArea(600.0)
        landparcel2.setCommune("CommuneB")
        landparcel2.setGeodesyRegistrationDistrictNumber("GRD2")
        landparcel2.setLandparcelNumber("LP2")

        List<Landparcel> landparcelList = [landparcel1, landparcel2]
        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findByFarmId(farm.getId()) >> landparcelList

        when:
        List<LandparcelDTO> result = landparcelFacade.getLandparcels(null, null, maxArea)

        then:
        result.size() == 1
        result[0].getArea() == 500.0
    }

    def "should filter landparcels by minimum and maximum area"() {
        given:
        Farm farm = Mock(Farm)
        LandOwnershipStatus ownerStatus = new LandOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
        Double minArea = 550.0
        Double maxArea = 650.0

        Landparcel landparcel1 = new Landparcel()
        landparcel1.setId(new LandparcelId(1, farm.getId()))
        landparcel1.setLandOwnershipStatus(ownerStatus)
        landparcel1.setArea(500.0)
        landparcel1.setCommune("CommuneA")
        landparcel1.setGeodesyRegistrationDistrictNumber("GRD1")
        landparcel1.setLandparcelNumber("LP1")

        Landparcel landparcel2 = new Landparcel()
        landparcel2.setId(new LandparcelId(2, farm.getId()))
        landparcel2.setLandOwnershipStatus(ownerStatus)
        landparcel2.setArea(600.0)
        landparcel2.setCommune("CommuneB")
        landparcel2.setGeodesyRegistrationDistrictNumber("GRD2")
        landparcel2.setLandparcelNumber("LP2")

        Landparcel landparcel3 = new Landparcel()
        landparcel3.setId(new LandparcelId(3, farm.getId()))
        landparcel3.setLandOwnershipStatus(ownerStatus)
        landparcel3.setArea(700.0)
        landparcel3.setCommune("CommuneC")
        landparcel3.setGeodesyRegistrationDistrictNumber("GRD3")
        landparcel3.setLandparcelNumber("LP3")

        List<Landparcel> landparcelList = [landparcel1, landparcel2, landparcel3]
        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findByFarmId(farm.getId()) >> landparcelList

        when:
        List<LandparcelDTO> result = landparcelFacade.getLandparcels(null, minArea, maxArea)

        then:
        result.size() == 1
        result[0].getArea() == 600.0
    }

    def "should return an empty list when search string is too short"() {
        given:
        Farm farm = Mock(Farm)
        String searchString = "ab" 
        LandOwnershipStatus ownerStatus = new LandOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)

        Landparcel landparcel1 = new Landparcel()
        landparcel1.setId(new LandparcelId(1, farm.getId()))
        landparcel1.setLandOwnershipStatus(ownerStatus)
        landparcel1.setArea(500.0)
        landparcel1.setCommune("CommuneA")
        landparcel1.setGeodesyRegistrationDistrictNumber("GRD1")
        landparcel1.setLandparcelNumber("LP1")

        Landparcel landparcel2 = new Landparcel()
        landparcel2.setId(new LandparcelId(2, farm.getId()))
        landparcel2.setLandOwnershipStatus(ownerStatus)
        landparcel2.setArea(600.0)
        landparcel2.setCommune("CommuneB")
        landparcel2.setGeodesyRegistrationDistrictNumber("GRD2")
        landparcel2.setLandparcelNumber("LP2")

        List<Landparcel> landparcelList = [landparcel1, landparcel2]
        userService.getLoggedUserFarm() >> farm
        landparcelRepository.findByFarmId(farm.getId()) >> landparcelList

        when:
        List<LandparcelDTO> result = landparcelFacade.getLandparcels(searchString, null, null)

        then:
        result.size() == 2 
    }

}