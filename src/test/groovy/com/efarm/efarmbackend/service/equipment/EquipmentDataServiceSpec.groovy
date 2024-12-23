package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO
import com.efarm.efarmbackend.repository.equipment.EquipmentCategoryRepository
import com.efarm.efarmbackend.service.equipment.EquipmentDisplayDataService
import spock.lang.Specification
import spock.lang.Subject

class EquipmentDisplayDataServiceSpec extends Specification {

    def equipmentCategoryRepository = Mock(EquipmentCategoryRepository)

    @Subject
    EquipmentDisplayDataService equipmentDisplayDataService = new EquipmentDisplayDataService(
            equipmentCategoryRepository
    )

    def setup() {
        setField(equipmentDisplayDataService, "categoryFieldsCache", new HashMap<>())
        setField(equipmentDisplayDataService, "cachedCategoryList", new ArrayList<>())
    }

    def "should initialize cache with categories and fields"() {
        given:
        List<String> categories = ['Ciągniki rolnicze', 'Kosiarki']
        equipmentCategoryRepository.findAllCategoryNames() >> categories

        equipmentDisplayDataService.metaClass.getFieldsForCategory('Ciągniki rolnicze') >> ['power', 'insurancePolicyNumber', 'insuranceExpirationDate', 'inspectionExpireDate']
        equipmentDisplayDataService.metaClass.getFieldsForCategory('Kosiarki') >> ['workingWidth']

        when:
        equipmentDisplayDataService.initializeCache()

        then:
        equipmentDisplayDataService.categoryFieldsCache.size() == 2
        equipmentDisplayDataService.categoryFieldsCache['Ciągniki rolnicze'] == ['equipmentName', 'category', 'brand', 'model', 'power', 'insurancePolicyNumber', 'insuranceExpirationDate', 'inspectionExpireDate']
        equipmentDisplayDataService.categoryFieldsCache['Kosiarki'] == ['equipmentName', 'category', 'brand', 'model', 'workingWidth']

        equipmentDisplayDataService.cachedCategoryList.size() == 2
        List<EquipmentCategoryDTO> categoryList = equipmentDisplayDataService.cachedCategoryList
        categoryList.any { it.categoryName == 'Ciągniki rolnicze' && it.fields == ['equipmentName', 'category', 'brand', 'model', 'power', 'insurancePolicyNumber', 'insuranceExpirationDate', 'inspectionExpireDate'] }
        categoryList.any { it.categoryName == 'Kosiarki' && it.fields == ['equipmentName', 'category', 'brand', 'model', 'workingWidth'] }
    }


    def "should return correct fields for tractors"() {
        given:
        String categoryName = 'Ciągniki rolnicze'

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ['power', 'insurancePolicyNumber', 'insuranceExpirationDate', 'inspectionExpireDate']
    }

    def "should return correct fields for trailers"() {
        given:
        String categoryName = 'Przyczepy rolnicze'

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ['capacity', 'insurancePolicyNumber', 'insuranceExpirationDate', 'inspectionExpireDate']
    }

    def "should return correct fields for harvesters"() {
        given:
        String categoryName = 'Kombajny zbożowe'

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ['power', 'capacity', 'workingWidth', 'insurancePolicyNumber', 'insuranceExpirationDate', 'inspectionExpireDate']
    }

    def "should return correct fields for agricultural machinery"() {
        given:
        String categoryName = 'Kosiarki'

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ['workingWidth']
    }

    def "should return correct fields for sprayers and spreaders"() {
        given:
        String categoryName = 'Opryskiwacze polowe'

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == ['capacity', 'workingWidth']
    }

    def "should return an empty list for unrecognized category"() {
        given:
        String categoryName = 'Unknown category'

        when:
        List<String> result = equipmentDisplayDataService.getFieldsForCategory(categoryName)

        then:
        result == []
    }

    def "should get all categories with fields"() {
        given:
        List<String> categories = ['Ciągniki rolnicze', 'Kosiarki']
        equipmentCategoryRepository.findAllCategoryNames() >> categories

        equipmentDisplayDataService.metaClass.getFieldsForCategory('Ciągniki rolnicze') >> ['power', 'insurancePolicyNumber', 'insuranceExpirationDate', 'inspectionExpireDate']
        equipmentDisplayDataService.metaClass.getFieldsForCategory('Kosiarki') >> ['workingWidth']

        List<String> commonFields = ['equipmentName', 'category', 'brand', 'model']

        equipmentDisplayDataService.initializeCache()
        when:
        List<EquipmentCategoryDTO> result = equipmentDisplayDataService.getAllCategoriesWithFields()

        then:
        result.size() == 2

        result[0].getCategoryName() == 'Ciągniki rolnicze'
        result[0].getFields() == commonFields + ['power', 'insurancePolicyNumber', 'insuranceExpirationDate', 'inspectionExpireDate']

        result[1].getCategoryName() == 'Kosiarki'
        result[1].getFields() == commonFields + ['workingWidth']
    }


    //helper function

    private void setField(Object target, String fieldName, Object value) {
        def field = target.getClass().getDeclaredField(fieldName)
        field.setAccessible(true)
        field.set(target, value)
    }

}
