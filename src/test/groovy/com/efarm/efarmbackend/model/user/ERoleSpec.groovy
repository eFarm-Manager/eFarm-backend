package com.efarm.efarmbackend.model.user

import spock.lang.Specification

class ERoleSpec extends Specification {

    // checking if ERole contains only expexted values
    def "should contain only the expected enum values - ERole enum"() {
        given:
        Set<String> expectedValues = ['ROLE_FARM_MANAGER', 'ROLE_FARM_EQUIPMENT_OPERATOR', 'ROLE_FARM_OWNER']

        when:
        Set<String> actualValues = ERole.values().collect { it.name() }

        then:
        actualValues == expectedValues
        actualValues.containsAll(expectedValues)
        expectedValues.containsAll(actualValues)
    }

}
