package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.equipment.EquipmentCategory;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest
import com.efarm.efarmbackend.service.equipment.FarmEquipmentService;
import com.efarm.efarmbackend.service.equipment.EquipmentDisplayDataService
import spock.lang.Specification
import spock.lang.Subject
import java.time.LocalDate


class FarmEquipmentServiceSpec extends Specification {

    def equipmentDisplayDataService = Mock(EquipmentDisplayDataService)

    @Subject
    FarmEquipmentService farmEquipmentService = new FarmEquipmentService(
        equipmentDisplayDataService: equipmentDisplayDataService
    )

    FarmEquipment equipment = Mock(FarmEquipment)

    def setup() {
        equipment.getId() >> Mock(FarmEquipmentId) {
            getId() >> 1
            getFarmId() >> 1
        }
        equipment.getEquipmentName() >> "Tractor X"
        equipment.getCategory() >> Mock(EquipmentCategory) { getCategoryName() >> "Ciągniki rolnicze" }
        equipment.getBrand() >> "Brand X"
        equipment.getModel() >> "Model X"
        equipment.getPower() >> 120
        equipment.getCapacity() >> 3000
        equipment.getWorkingWidth() >> 5.5
        equipment.getInsurancePolicyNumber() >> "78156"
        equipment.getInsuranceExpirationDate() >> LocalDate.of(2025, 12, 31)
        equipment.getInspectionExpireDate() >> LocalDate.of(2024, 12, 31)
    }

    def "should create DTO with all fields displayed"() {
        given:
        List<String> fieldsToDisplay = ["power", "capacity", "workingWidth", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]

        when:
        AddUpdateFarmEquipmentRequest result = farmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fieldsToDisplay)

        then:
        result.getEquipmentId() == 1
        result.getEquipmentName() == "Tractor X"
        result.getCategory() == "Ciągniki rolnicze"
        result.getBrand() == "Brand X"
        result.getModel() == "Model X"
        result.getPower() == 120
        result.getCapacity() == 3000
        result.getWorkingWidth() == 5.5
        result.getInsurancePolicyNumber() == "78156"
        result.getInsuranceExpirationDate() == LocalDate.of(2025, 12, 31)
        result.getInspectionExpireDate() == LocalDate.of(2024, 12, 31)
    }

    def "should create DTO with only power and capacity fields displayed"() {
        given:
        List<String> fieldsToDisplay = ["power", "capacity"]

        when:
        AddUpdateFarmEquipmentRequest result = farmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fieldsToDisplay)

        then:
        result.getEquipmentId() == 1
        result.getEquipmentName() == "Tractor X"
        result.getCategory() == "Ciągniki rolnicze"
        result.getBrand() == "Brand X"
        result.getModel() == "Model X"
        result.getPower() == 120
        result.getCapacity() == 3000
        result.getWorkingWidth() == null
        result.getInsurancePolicyNumber() == null
        result.getInsuranceExpirationDate() == null
        result.getInspectionExpireDate() == null
    }

    def "should create DTO with only insurance-related fields displayed"() {
        given:
        List<String> fieldsToDisplay = ["insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]

        when:
        AddUpdateFarmEquipmentRequest result = farmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fieldsToDisplay)

        then:
        result.getEquipmentId() == 1
        result.getEquipmentName() == "Tractor X"
        result.getCategory() == "Ciągniki rolnicze"
        result.getBrand() == "Brand X"
        result.getModel() == "Model X"
        result.getPower() == null
        result.getCapacity() == null
        result.getWorkingWidth() == null
        result.getInsurancePolicyNumber() == "78156"
        result.getInsuranceExpirationDate() == LocalDate.of(2025, 12, 31)
        result.getInspectionExpireDate() == LocalDate.of(2024, 12, 31)
    }

    def "should create DTO with no fields displayed"() {
        given:
        List<String> fieldsToDisplay = []

        when:
        AddUpdateFarmEquipmentRequest result = farmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fieldsToDisplay)

        then:
        result.getEquipmentId() == 1
        result.getEquipmentName() == "Tractor X"
        result.getCategory() == "Ciągniki rolnicze"
        result.getBrand() == "Brand X"
        result.getModel() == "Model X"
        result.getPower() == null
        result.getCapacity() == null
        result.getWorkingWidth() == null
        result.getInsurancePolicyNumber() == null
        result.getInsuranceExpirationDate() == null
        result.getInspectionExpireDate() == null
    }

    def "should set specific fields for category"() {
        given:
        String categoryName = "Ciągniki rolnicze"
        AddUpdateFarmEquipmentRequest farmEquipmentDTO = Mock(AddUpdateFarmEquipmentRequest) {
            getPower() >> 120
            getCapacity() >> null
            getWorkingWidth() >> null
            getInsurancePolicyNumber() >> "78156"
            getInsuranceExpirationDate() >> LocalDate.of(2025, 12, 31)
            getInspectionExpireDate() >> LocalDate.of(2024, 12, 31)
        }

        FarmEquipment equipment = new FarmEquipment()

        List<String> fields = ["power", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]
        equipmentDisplayDataService.getFieldsForCategory(categoryName) >> fields

        when:
        farmEquipmentService.setSpecificFieldsForCategory(farmEquipmentDTO, equipment, categoryName)

        then:
        equipment.getPower() == 120
        equipment.getCapacity() == null 
        equipment.getCapacity() == null
        equipment.getInsurancePolicyNumber() == "78156"
        equipment.getInsuranceExpirationDate() == LocalDate.of(2025, 12, 31)
        equipment.getInspectionExpireDate() == LocalDate.of(2024, 12, 31)
    }

    def "should set common fields"() {
        given:
        AddUpdateFarmEquipmentRequest farmEquipmentDTO = Mock(AddUpdateFarmEquipmentRequest) {
            getEquipmentName() >> "Tractor X"
            getBrand() >> null
            getModel() >> "Model X"
        }
        FarmEquipment equipment = new FarmEquipment()

        when:
        farmEquipmentService.setCommonFieldsForCategory(farmEquipmentDTO, equipment)

        then:
        equipment.getEquipmentName() == "Tractor X"
        equipment.getBrand() == null 
        equipment.getModel() == "Model X"  
    }
}