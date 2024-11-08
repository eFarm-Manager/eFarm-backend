package com.efarm.efarmbackend.service.facades

import com.efarm.efarmbackend.model.equipment.FarmEquipment
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId
import com.efarm.efarmbackend.model.equipment.EquipmentCategory
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.service.equipment.FarmEquipmentFacade
import org.springframework.http.HttpStatus
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository
import com.efarm.efarmbackend.service.equipment.EquipmentDisplayDataService
import com.efarm.efarmbackend.service.equipment.FarmEquipmentService
import com.efarm.efarmbackend.repository.equipment.EquipmentCategoryRepository;
import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.user.UserService
import org.springframework.http.HttpStatus
import com.efarm.efarmbackend.payload.response.MessageResponse;
import spock.lang.Specification
import spock.lang.Subject
import java.time.LocalDate

class FarmEquipmentFacadeSpec extends Specification {

    def farmEquipmentRepository = Mock(FarmEquipmentRepository)
    def userService = Mock(UserService)
    def equipmentDisplayDataService = Mock(EquipmentDisplayDataService)
    def farmEquipmentService = Mock(FarmEquipmentService)
    def equipmentCategoryRepository = Mock(EquipmentCategoryRepository)

    @Subject
    FarmEquipmentFacade farmEquipmentFacade = new FarmEquipmentFacade(
            farmEquipmentRepository: farmEquipmentRepository,
            userService: userService,
            equipmentDisplayDataService: equipmentDisplayDataService,
            farmEquipmentService: farmEquipmentService,
            equipmentCategoryRepository: equipmentCategoryRepository,
    )
    /*
        getFarmEquipment
    */
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
        List<EquipmentSummaryDTO> result = farmEquipmentFacade.getFarmEquipment(searchQuery)

        then:
        result.size() == 1
        result.equipmentName == ["Tractor"]
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
        List<EquipmentSummaryDTO> result = farmEquipmentFacade.getFarmEquipment(searchQuery)

        then:
        result.size() == 1
        result.equipmentName == ["Tractor"]
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
        List<EquipmentSummaryDTO> result = farmEquipmentFacade.getFarmEquipment(searchQuery)

        then:
        result.size() == 0
        result.equipmentName == []
    }
    /*
        getEquipmentDetails
    */
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
            getIsAvailable() >> true
        }
        List<String> fields = ["power", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.of(equipment)
        equipmentDisplayDataService.getFieldsForCategory(equipment.getCategory().getCategoryName()) >> fields

        farmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fields) >> Mock(AddUpdateFarmEquipmentRequest) {
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
        AddUpdateFarmEquipmentRequest response = farmEquipmentFacade.getEquipmentDetails(equipmentId)

        then:
        response.equipmentId == equipmentId
        response.equipmentName == "Tractor X"
        response.category == "Ciągniki rolnicze"
        response.brand == "Brand X"
        response.model == "Model X"
        response.power == 120
        response.capacity == null
        response.workingWidth == null
        response.insurancePolicyNumber == "78156"
        response.insuranceExpirationDate == LocalDate.of(2025, 12, 31)
        response.inspectionExpireDate == LocalDate.of(2024, 12, 31)
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
        farmEquipmentFacade.getEquipmentDetails(equipmentId)

        then:
        RuntimeException e = thrown(RuntimeException)
        e.message == "Nie znaleziono maszyny o id: ${equipmentId}"
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
            getIsAvailable() >> true
        }

        List<String> fields = ["power", "capacity", "workingWidth"]

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.of(equipment)
        equipmentDisplayDataService.getFieldsForCategory(equipment.getCategory().getCategoryName()) >> fields

        farmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fields) >> Mock(AddUpdateFarmEquipmentRequest) {
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
        AddUpdateFarmEquipmentRequest response = farmEquipmentFacade.getEquipmentDetails(equipmentId)

        then:
        response.equipmentId == equipmentId
        response.equipmentName == "Tractor X"
        response.category == "Ciągniki rolnicze"
        response.brand == "Brand X"
        response.model == "Model X"
        response.power == 120
        response.capacity == null
        response.workingWidth == 5.5
    }
    /*
        addNewFarmEquipment
    */
    def "should add new farming equipment"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 5
        }
        AddUpdateFarmEquipmentRequest farmEquipmentDTO = Mock(AddUpdateFarmEquipmentRequest) {
            getEquipmentName() >> "Tractor X"
            getCategory() >> "Ciągniki rolnicze"
            getBrand() >> "Brand X"
            getModel() >> "Model X"
            getPower() >> 120
            getCapacity() >> null
            getWorkingWidth() >> 5.5
        }
        EquipmentCategory equipmentCategory = Mock(EquipmentCategory) {
            getCategoryName() >> "Ciągniki rolnicze"
        }

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findNextFreeIdForFarm(farm.getId()) >> 1
        equipmentCategoryRepository.findByCategoryName(farmEquipmentDTO.getCategory()) >> equipmentCategory
        farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(farmEquipmentDTO.getEquipmentName(), farm) >> false

        FarmEquipment equipment = new FarmEquipment(new FarmEquipmentId(1, farm.getId()), equipmentCategory, farm)

        farmEquipmentService.setCommonFieldsForCategory(farmEquipmentDTO, equipment) >> { /* No-op */ }
        farmEquipmentService.setSpecificFieldsForCategory(farmEquipmentDTO, equipment, farmEquipmentDTO.getCategory()) >> { /* No-op */ }

        when:
     	MessageResponse response = farmEquipmentFacade.addNewFarmEquipment(farmEquipmentDTO)

        then:
        1 * farmEquipmentRepository.save(_ as FarmEquipment)
        response.message == "Pomyślnie dodano nową maszynę"     
    }

    def "should return error if equipment with the same name already exists"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 5
        }
        AddUpdateFarmEquipmentRequest farmEquipmentDTO = Mock(AddUpdateFarmEquipmentRequest) {
            getEquipmentName() >> "Tractor X"
            getCategory() >> "Ciągniki rolnicze"
        }

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(farmEquipmentDTO.getEquipmentName(), farm) >> true

        when:
       	farmEquipmentFacade.addNewFarmEquipment(farmEquipmentDTO)

        then:
	RuntimeException e = thrown(RuntimeException)
	e.message == "Maszyna o podanej nazwie już istnieje"
        0 * farmEquipmentRepository.save(_)
    }
    /*
        updateFarmEquipment
    */

    def "should update farm equipment successfully"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 5
        }
        AddUpdateFarmEquipmentRequest farmEquipmentDTO = Mock(AddUpdateFarmEquipmentRequest) {
            getEquipmentName() >> "Tractor X"
            getCategory() >> "Ciągniki rolnicze"
            getBrand() >> "Brand X"
            getModel() >> "Model X"
            getPower() >> 120
            getCapacity() >> null
            getWorkingWidth() >> 5.5
        }
        FarmEquipment equipment = Mock(FarmEquipment) {
            getIsAvailable() >> true
            getCategory() >> Mock(EquipmentCategory) {
                getCategoryName() >> "Ciągniki rolnicze"
            }
        }
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, farm.getId())

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.of(equipment)
        farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(farmEquipmentDTO.getEquipmentName(), farm) >> false

        when:
        MessageResponse response = farmEquipmentFacade.updateFarmEquipment(1, farmEquipmentDTO)

        then:
        1 * farmEquipmentService.setCommonFieldsForCategory(farmEquipmentDTO, equipment)
        1 * farmEquipmentService.setSpecificFieldsForCategory(farmEquipmentDTO, equipment, "Ciągniki rolnicze")
        1 * farmEquipmentRepository.save(equipment)
        response.message == "Pomyślnie zaktualizowane dane maszyny."
    }

    def "should return error when equipment is not found"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 5
        }
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, farm.getId())

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.empty()

        when:
        MessageResponse response = farmEquipmentFacade.updateFarmEquipment(1, Mock(AddUpdateFarmEquipmentRequest))

        then:
	RuntimeException e = thrown(RuntimeException)
	e.message == "Nie znaleziono maszyny o id: 1"
        0 * farmEquipmentRepository.save(_)
    }

    def "should return error when equipment is already available with the same name"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 5
        }
        AddUpdateFarmEquipmentRequest farmEquipmentDTO = Mock(AddUpdateFarmEquipmentRequest) {
            getEquipmentName() >> "Tractor X"
        }
        FarmEquipment equipment = Mock(FarmEquipment) {
            getIsAvailable() >> true
        }
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, farm.getId())

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.of(equipment)
        farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(farmEquipmentDTO.getEquipmentName(), farm) >> true

        when:
        MessageResponse response = farmEquipmentFacade.updateFarmEquipment(1, farmEquipmentDTO)

        then:
	RuntimeException e = thrown(RuntimeException)
	e.message == "Maszyna o podanej nazwie już występuje w gospodarstwie"
        0 * farmEquipmentRepository.save(_)
    }

    def "should return error when equipment is not available"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 5
        }
        AddUpdateFarmEquipmentRequest farmEquipmentDTO = Mock(AddUpdateFarmEquipmentRequest) {
            getEquipmentName() >> "Tractor X"
        }
        FarmEquipment equipment = Mock(FarmEquipment) {
            getIsAvailable() >> false
        }
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, farm.getId())

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.of(equipment)

        when:
        farmEquipmentFacade.updateFarmEquipment(1, farmEquipmentDTO)

        then:
	RuntimeException e = thrown(RuntimeException)
	e.message == "Wybrany sprzęt już nie istnieje"
        0 * farmEquipmentRepository.save(_)
    }

    /*
        deleteFarmEquipment
    */

    def "should delete farm equipment successfully"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 5
        }
        FarmEquipment equipment = Mock(FarmEquipment) {
            getIsAvailable() >> true
        }
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, farm.getId())

        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.of(equipment)

        when:
        MessageResponse response = farmEquipmentFacade.deleteFarmEquipment(1)

        then:
        1 * equipment.setIsAvailable(false)
        1 * farmEquipmentRepository.save(equipment)
        response.message == "Pomyślnie usunięto maszynę"
    }

    def "should return error when equipment is not found"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 5
        }
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, farm.getId())
    
        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.empty()
    
        when:
        farmEquipmentFacade.deleteFarmEquipment(1)
    
        then:
	RuntimeException e = thrown(RuntimeException)
	e.message == "Nie znaleziono maszyny o id: 1"
        0 * farmEquipmentRepository.save(_)
    }
    
    def "should return error when equipment is already deleted"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 5
        }
        FarmEquipment equipment = Mock(FarmEquipment) {
            getIsAvailable() >> false
        }
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, farm.getId())
    
        userService.getLoggedUserFarm() >> farm
        farmEquipmentRepository.findById(farmEquipmentId) >> Optional.of(equipment)
    
        when:
        farmEquipmentFacade.deleteFarmEquipment(1)
    
        then:
	RuntimeException e = thrown(RuntimeException)
	e.message == "Wybrana maszyna została już usunięta"
        0 * farmEquipmentRepository.save(_)
    }



}
