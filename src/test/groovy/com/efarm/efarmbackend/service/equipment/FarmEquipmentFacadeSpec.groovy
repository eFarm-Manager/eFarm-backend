package com.efarm.efarmbackend.service.facades

import com.efarm.efarmbackend.model.equipment.FarmEquipment
import com.efarm.efarmbackend.model.equipment.FarmEquipmentDTO
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId
import com.efarm.efarmbackend.model.equipment.EquipmentCategory
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.service.equipment.FarmEquipmentFacade
import org.springframework.http.HttpStatus
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository
import com.efarm.efarmbackend.service.equipment.EquipmentDisplayDataService
import com.efarm.efarmbackend.service.equipment.FarmEquipmentService
import com.efarm.efarmbackend.service.user.UserService
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Subject
import java.time.LocalDate

class FarmEquipmentFacadeSpec extends Specification {

    def farmEquipmentRepository = Mock(FarmEquipmentRepository)
    def userService = Mock(UserService)
    def equipmentDisplayDataService = Mock(EquipmentDisplayDataService)
    def farmEquipmentService = Mock(FarmEquipmentService)

    @Subject
    FarmEquipmentFacade farmEquipmentFacade = new FarmEquipmentFacade(
            farmEquipmentRepository: farmEquipmentRepository,
            userService: userService,
            equipmentDisplayDataService: equipmentDisplayDataService,
            farmEquipmentService: farmEquipmentService
    )

    def "should return all without any search query"() {
        given:
        String searchQuery = null
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        FarmEquipmentId equipmentId1 = Mock(FarmEquipmentId) {
            getFarmId() >> farmId
        }
        FarmEquipmentId equipmentId2 = Mock(FarmEquipmentId) {
            getFarmId() >> farmId
        }
        FarmEquipment equipment1 = Mock(FarmEquipment) {
            getEquipmentName() >> "Tractor"
            getBrand() >> "John Deere"
            getCategory() >> Mock(EquipmentCategory) { getCategoryName() >> "Agriculture" }
            getIsAvailable() >> true
            getId() >> equipmentId1
        }
        FarmEquipment equipment2 = Mock(FarmEquipment) {
            getEquipmentName() >> "Harvester"
            getBrand() >> "Case"
            getCategory() >> Mock(EquipmentCategory) { getCategoryName() >> "Harvesting" }
            getIsAvailable() >> false
            getId() >> equipmentId2
        }

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findByFarmIdFarm_Id(farmId) >> [equipment1, equipment2]

        when:
        ResponseEntity<List<FarmEquipmentDTO>> result = farmEquipmentFacade.getFarmEquipment(searchQuery)

        then:
        result.getStatusCode() == HttpStatus.OK
        result.body.size() == 1
        result.body.equipmentName == ["Tractor"]
    }

    def "should return all with search query case that will get brand"() {
        given:
        String searchQuery = "John"
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        FarmEquipmentId equipmentId1 = Mock(FarmEquipmentId) {
            getFarmId() >> farmId
        }
        FarmEquipmentId equipmentId2 = Mock(FarmEquipmentId) {
            getFarmId() >> farmId
        }
        FarmEquipment equipment1 = Mock(FarmEquipment) {
            getEquipmentName() >> "Tractor"
            getBrand() >> "John Deere"
            getCategory() >> Mock(EquipmentCategory) { getCategoryName() >> "Agriculture" }
            getIsAvailable() >> true
            getId() >> equipmentId1
        }
        FarmEquipment equipment2 = Mock(FarmEquipment) {
            getEquipmentName() >> "Harvester"
            getBrand() >> "Case"
            getCategory() >> Mock(EquipmentCategory) { getCategoryName() >> "Harvesting" }
            getIsAvailable() >> false
            getId() >> equipmentId2
        }

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findByFarmIdFarm_Id(farmId) >> [equipment1, equipment2]

        when:
        ResponseEntity<List<FarmEquipmentDTO>> result = farmEquipmentFacade.getFarmEquipment(searchQuery)

        then:
        result.getStatusCode() == HttpStatus.OK
        result.body.size() == 1
        result.body.equipmentName == ["Tractor"]
    }

    def "should return nothing with search query doesnt exist since it doesnt exist"() {
        given:
        String searchQuery = "doesnt exist"
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        FarmEquipmentId equipmentId1 = Mock(FarmEquipmentId) {
            getFarmId() >> farmId
        }
        FarmEquipmentId equipmentId2 = Mock(FarmEquipmentId) {
            getFarmId() >> farmId
        }
        FarmEquipment equipment1 = Mock(FarmEquipment) {
            getEquipmentName() >> "Tractor"
            getBrand() >> "John Deere"
            getCategory() >> Mock(EquipmentCategory) { getCategoryName() >> "Agriculture" }
            getIsAvailable() >> true
            getId() >> equipmentId1
        }
        FarmEquipment equipment2 = Mock(FarmEquipment) {
            getEquipmentName() >> "Harvester"
            getBrand() >> "Case"
            getCategory() >> Mock(EquipmentCategory) { getCategoryName() >> "Harvesting" }
            getIsAvailable() >> false
            getId() >> equipmentId2
        }

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findByFarmIdFarm_Id(farmId) >> [equipment1, equipment2]

        when:
        ResponseEntity<List<FarmEquipmentDTO>> result = farmEquipmentFacade.getFarmEquipment(searchQuery)

        then:
        result.getStatusCode() == HttpStatus.OK
        result.body.size() == 0
        result.body.equipmentName == []
    }

    def "should return all tractor details fields"() {
        given:
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        Integer equipmentId = 2
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, farmId)
        FarmEquipment equipment = Mock(FarmEquipment) {
            getId() >> farmEquipmentId
            getEquipmentName() >> "Tractor X"
            getCategory() >> Mock(EquipmentCategory) { getCategoryName() >> "Ciągniki rolnicze" }
            getBrand() >> "Brand X"
            getModel() >> "Model X"
            getPower() >> 120
            getCapacity() >> 3000
            getWorkingWidth() >> 5.5
            getInsurancePolicyNumber() >> "78156"
            getInsuranceExpirationDate() >> LocalDate.of(2025, 12, 31)
            getInspectionExpireDate() >> LocalDate.of(2024, 12, 31)
        }
        List<String> fields = ["power", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.of(equipment)
        equipmentDisplayDataService.getFieldsForCategory(equipment.getCategory().getCategoryName()) >> fields

        farmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fields) >> Mock(FarmEquipmentDTO) {
            getEquipmentId() >> equipmentId
            getEquipmentName() >> "Tractor X"
            getCategory() >> "Ciągniki rolnicze"
            getBrand() >> "Brand X"
            getModel() >> "Model X"
            getPower() >> 120
            getCapacity() >> null
            getWorkingWidth() >> null
            getInsurancePolicyNumber() >> "78156"
            getInsuranceExpirationDate() >> LocalDate.of(2025, 12, 31)
            getInspectionExpireDate() >> LocalDate.of(2024, 12, 31)
        }

        when:
        ResponseEntity<?> response = farmEquipmentFacade.getEquipmentDetails(equipmentId)

        then:
        response.getStatusCode() == HttpStatus.OK
        response.body.equipmentId == equipmentId
        response.body.equipmentName == "Tractor X"
        response.body.category == "Ciągniki rolnicze"
        response.body.brand == "Brand X"
        response.body.model == "Model X"
        response.body.power == 120
        response.body.capacity == null
        response.body.workingWidth == null
        response.body.insurancePolicyNumber == "78156"
        response.body.insuranceExpirationDate == LocalDate.of(2025, 12, 31)
        response.body.inspectionExpireDate == LocalDate.of(2024, 12, 31)
    }

    def "should return 400 when equipment is not found"() {
        given:
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        Integer equipmentId = 2
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, farmId)

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.empty()

        when:
        ResponseEntity<?> response = farmEquipmentFacade.getEquipmentDetails(equipmentId)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Nie znaleziono maszyny o id: ${equipmentId}"
    }

    def "should handle case when some fields to display do not exist on equipment"() {
        given:
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        Integer equipmentId = 2
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, farmId)

        FarmEquipment equipment = Mock(FarmEquipment) {
            getId() >> farmEquipmentId
            getEquipmentName() >> "Tractor X"
            getCategory() >> Mock(EquipmentCategory) { getCategoryName() >> "Ciągniki rolnicze" }
            getBrand() >> "Brand X"
            getModel() >> "Model X"
            getPower() >> 120
            getCapacity() >> null
            getWorkingWidth() >> 5.5
        }

        List<String> fields = ["power", "capacity", "workingWidth"]

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.of(equipment)
        equipmentDisplayDataService.getFieldsForCategory(equipment.getCategory().getCategoryName()) >> fields

        farmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fields) >> Mock(FarmEquipmentDTO) {
            getEquipmentId() >> equipmentId
            getEquipmentName() >> "Tractor X"
            getCategory() >> "Ciągniki rolnicze"
            getBrand() >> "Brand X"
            getModel() >> "Model X"
            getPower() >> 120
            getCapacity() >> null
            getWorkingWidth() >> 5.5
        }

        when:
        ResponseEntity<?> response = farmEquipmentFacade.getEquipmentDetails(equipmentId)

        then:
        response.getStatusCode() == HttpStatus.OK
        response.body.equipmentId == equipmentId
        response.body.equipmentName == "Tractor X"
        response.body.category == "Ciągniki rolnicze"
        response.body.brand == "Brand X"
        response.body.model == "Model X"
        response.body.power == 120
        response.body.capacity == null
        response.body.workingWidth == 5.5
    }
}
