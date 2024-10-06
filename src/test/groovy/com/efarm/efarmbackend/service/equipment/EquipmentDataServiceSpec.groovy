package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.service.equipment.EquipmentDisplayDataService;
import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.repository.equipment.EquipmentCategoryRepository;
import spock.lang.Specification
import spock.lang.Subject


class EquipmentDisplayDataServiceSpec extends Specification {

    def equipmentCategoryRepository = Mock(EquipmentCategoryRepository)

    @Subject
    EquipmentDisplayDataService equipmentDisplayDataService = new EquipmentDisplayDataService(
        equipmentCategoryRepository: equipmentCategoryRepository
    )

    def "should return correct fields for tractors"() {
        given:
        String categoryName = "Ciągniki rolnicze"

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ["power", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]
    }

    def "should return correct fields for trailers"() {
        given:
        String categoryName = "Przyczepy rolnicze"

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ["capacity", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]
    }

    def "should return correct fields for harvesters"() {
        given:
        String categoryName = "Kombajny zbożowe"

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ["power", "capacity", "workingWidth", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]
    }

    def "should return correct fields for agricultural machinery"() {
        given:
        String categoryName = "Kosiarki"

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ["workingWidth"]
    }

    def "should return correct fields for sprayers and spreaders"() {
        given:
        String categoryName = "Opryskiwacze polowe"

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ["capacity", "workingWidth"]
    }

    def "should return an empty list for unrecognized category"() {
        given:
        String categoryName = "Unknown category"

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == []
    }

    def "should get all categoires with fields"() {
        given:
        List<String> categories = ["Ciągniki rolnicze", "Kosiarki"]
        equipmentCategoryRepository.findAllCategoryNames() >> categories

        equipmentDisplayDataService.getFieldsForCategory("Ciągniki rolnicze") >> ["power", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]
        equipmentDisplayDataService.getFieldsForCategory("Kosiarki") >> ["workingWidth"]

        List<String> commonFields = ["equipmentName", "category", "brand", "model"]

        when:
        List<EquipmentCategoryDTO> result = equipmentDisplayDataService.getAllCategoriesWithFields()

        then:
        result.size() == 2

        result[0].getCategoryName() == "Ciągniki rolnicze"
        result[0].getFields() == commonFields + ["power", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate"]

        result[1].getCategoryName() == "Kosiarki"
        result[1].getFields() == commonFields + ["workingWidth"]  
    }
}