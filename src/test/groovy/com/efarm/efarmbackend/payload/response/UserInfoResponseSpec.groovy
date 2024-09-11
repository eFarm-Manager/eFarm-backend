package com.efarm.efarmbackend.payload.response

import spock.lang.Specification

class UserInfoResponseSpec extends Specification {

    // checking setRoles for null
    def "should handle null roles - setRoles"() {
        given:
        UserInfoResponse response = new UserInfoResponse(1, "user1", "user1@gmail.com", ["ROLE_FARM_MANAGER"])

        when:
        response.setRoles(null)

        then:
        response.getRoles().isEmpty()
    }

    // checking getRoles function
    def "should return a new list - getRoles"() {
        given:
        List<String> initialRoles = ["ROLE_FARM_MANAGER"]
        UserInfoResponse response = new UserInfoResponse(1, "user1", "user1@gmail.com", initialRoles)

        when:
        List<String> roles = response.getRoles()
        roles.add("ROLE_FARM_EQUIPMENT_OPERATOR")

        then:
        response.getRoles().containsAll(initialRoles)
        !response.getRoles().contains("ROLE_FARM_EQUIPMENT_OPERATOR")
    }

    //initializing UserInfoResponse with null roles and then setting them
    def "should properly initialize roles when setRoles is provided with a non-null list"() {
        given:
        List<String> roles = ["ROLE_FARM_MANAGER", "ROLE_FARM_EQUIPMENT_OPERATOR"]
        UserInfoResponse response = new UserInfoResponse(1, "user1", "user1@gmail.com", [])

        when:
        response.setRoles(roles)

        then:
        response.getRoles() == roles
    }

    //i dont see any handling incorrect roels here so not testing that
}
