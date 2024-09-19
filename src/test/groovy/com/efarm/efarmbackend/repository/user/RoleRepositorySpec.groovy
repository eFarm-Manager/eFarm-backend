package com.efarm.efarmbackend.repository.user

import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.ERole
import spock.lang.Specification

class RoleRepositorySpec extends Specification {

    RoleRepository roleRepository = Mock(RoleRepository)
    String roleNameManager = 'ROLE_FARM_MANAGER'
    String roleOperator = 'ROLE_FARM_EQUIPMENT_OPERATOR'
    Role class_role_manager = Mock(Role) {
        getId() >> 1
        getName() >> ERole.valueOf(roleNameManager)
    }
    Role class_role_operator = Mock(Role) {
        getId() >> 2
        getName() >> ERole.valueOf(roleOperator)
    }

    // checking if findByName function correctly returns ROLE_FARM_MANAGER
    def "should correctly return role ROLE_FARM_MANAGER - findByName"() {
        given:
        roleRepository.findByName(ERole.valueOf(roleNameManager)) >> Optional.of(class_role_manager)

        when:
        Optional<Role> foundRole = roleRepository.findByName(ERole.valueOf(roleNameManager))

        then:
        foundRole.isPresent()
        foundRole.get() == class_role_manager
        foundRole.get().name == class_role_manager.name
    }

    // checking if findByName function correctly returns ROLE_FARM_MANAGER
    def "should correctly return role ROLE_FARM_EQUIPMENT_OPERATOR - findByName"() {
        given:
        roleRepository.findByName(ERole.valueOf(roleOperator)) >> Optional.of(class_role_operator)

        when:
        Optional<Role> foundRole = roleRepository.findByName(ERole.valueOf(roleOperator))

        then:
        foundRole.isPresent()
        foundRole.get() == class_role_operator
        foundRole.get().name == class_role_operator.name
    }

    // when non existent role is passed should throw IllegalArgument Exception
    def "should throw IllegalArgumentException when an invalid role is passed - findByName"() {
        given:
        String nonExistentRole = 'ROLE_TEST'
        roleRepository.findByName(_ as ERole) >> Optional.empty()

        when:
        Optional<Role> foundRole = roleRepository.findByName(ERole.valueOf(nonExistentRole))

        then:
        thrown(IllegalArgumentException)
    }

    // when no role passed there is no role, null
    def "should return nothing when null role is passed - findByName"() {
        given:
        roleRepository.findByName(null) >> Optional.empty()

        when:
        Optional<Role> foundRole = roleRepository.findByName(null)

        then:
        !foundRole.isPresent()
    }
}
