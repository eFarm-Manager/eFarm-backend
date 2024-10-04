package com.efarm.efarmbackend.service;

import spock.lang.Specification
import spock.lang.Subject


class EquipmentDisplayDataServiceSpec extends Specification {

    @Subject
    EquipmentDisplayDataService equipmentDisplayDataService = new EquipmentDisplayDataService()

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
}