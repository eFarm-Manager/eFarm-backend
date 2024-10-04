package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.payload.request.UpdateFarmDetailsRequest
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.repository.user.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class FarmServiceSpec extends Specification {

    def farmRepository = Mock(FarmRepository)
    def activationCodeRepository = Mock(ActivationCodeRepository)
    def userRepository = Mock(UserRepository)

    @Subject
    FarmService farmService = new FarmService(
            farmRepository: farmRepository,
            activationCodeRepository: activationCodeRepository,
            userRepository: userRepository,
            frontendUriToUpdateActivationCode: "/updateActivationCode"
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    def "should handle creation of farm owner"() {
        given:
        String farmName = "uniqueName"
        Integer addressId = 1
        Integer activationCodeId = 1

        when:
        Farm newFarm = farmService.createFarm(farmName, addressId, activationCodeId)

        then:
        newFarm.getFarmName() == farmName
        newFarm.getIdAddress() == addressId
        newFarm.getIdActivationCode() == activationCodeId
    }

    def "should handle deactivation of expired activation codes"() {
        given:
        ActivationCode activationCode1 = Mock(ActivationCode)
        activationCode1.getId() >> 1
        activationCode1.getExpireDate() >> LocalDate.now().plusDays(10)

        ActivationCode activationCode2 = Mock(ActivationCode)
        activationCode2.getId() >> 2
        activationCode2.getExpireDate() >> LocalDate.now().minusDays(10)

        Farm farm1 = Mock(Farm)
        farm1.getIsActive() >> true
        farm1.getFarmName() >> "farm1"
        farm1.getIdActivationCode() >> 1

        Farm farm2 = Mock(Farm)
        farm2.getId() >> 2
        farm2.getIsActive() >> true
        farm2.getFarmName() >> "farm2"
        farm2.getIdActivationCode() >> 2

        farmRepository.findByIsActiveTrue() >> [farm1, farm2]

        activationCodeRepository.findById(1) >> Optional.of(activationCode1)
        activationCodeRepository.findById(2) >> Optional.of(activationCode2)

        when:
        farmService.deactivateFarmsWithExpiredActivationCodes()

        then:
        0 * farm1.setIsActive(false)
        1 * farm2.setIsActive(false)
        farm2.getIsActive() >>> [true, false]
    }

    def "should show that farm not active for owner"() {
        given:
        Role role_owner = Mock(Role)
        role_owner.getName() >> ERole.ROLE_FARM_OWNER
        Farm userFarm = Mock(Farm)
        userFarm.getId() >> 1
        userFarm.getIsActive() >> false

        when:
        ResponseEntity<?> response = farmService.checkFarmDeactivation(userFarm, role_owner)

        then:
        response.getStatusCode() == HttpStatus.FORBIDDEN
        response.getHeaders().getLocation() == URI.create("/updateActivationCode")
        response.getBody().message == "Gospodarstwo jest nieaktywne. Podaj nowy kod aktywacyjny."
    }

    def "should show that farm not active for manager"() {
        given:
        Role role_manager = Mock(Role)
        role_manager.getName() >> ERole.ROLE_FARM_MANAGER
        Farm userFarm = Mock(Farm)
        userFarm.getId() >> 1
        userFarm.getIsActive() >> false

        when:
        ResponseEntity<?> response = farmService.checkFarmDeactivation(userFarm, role_manager)

        then:
        response.getStatusCode() == HttpStatus.FORBIDDEN
        response.getBody().message == "Gospodarstwo jest nieaktywne. Kod aktywacyjny wygasł."
    }

    def "should show that farm not active for operator"() {
        given:
        Role role_operator = Mock(Role)
        role_operator.getName() >> ERole.ROLE_FARM_EQUIPMENT_OPERATOR
        Farm userFarm = Mock(Farm)
        userFarm.getId() >> 1
        userFarm.getIsActive() >> false

        when:
        ResponseEntity<?> response = farmService.checkFarmDeactivation(userFarm, role_operator)

        then:
        response.getStatusCode() == HttpStatus.FORBIDDEN
        response.getBody().message == "Gospodarstwo jest nieaktywne. Kod aktywacyjny wygasł."
    }

    def "should not show inactive message because farm is active"() {
        given:
        Role role_owner = Mock(Role)
        role_owner.getName() >> ERole.ROLE_FARM_OWNER
        Farm userFarm = Mock(Farm)
        userFarm.getId() >> 1
        userFarm.getIsActive() >> true

        when:
        ResponseEntity<?> response = farmService.checkFarmDeactivation(userFarm, role_owner)

        then:
        response == null
    }

    def "should update farm details - name, farm number and sanitary register number"() {
        given:
        Farm farm = new Farm()
        farm.setId(1)
        farm.setFarmName("Old Farm")
        farm.setFarmNumber("123")
        farm.setFeedNumber("456")
        farm.setSanitaryRegisterNumber("987")
        UpdateFarmDetailsRequest updateFarmDetailsRequest = new UpdateFarmDetailsRequest(
                farmName: "New Farm",
                farmNumber: "202",
                feedNumber: "456",
                sanitaryRegisterNumber: "101"
        )

        when:
        farmService.updateFarmDetails(farm, updateFarmDetailsRequest)

        then:
        1 * farmRepository.save(farm)
        farm.getFarmName() == "New Farm"
        farm.getFarmNumber() == "202"
        farm.getFeedNumber() == "456"
        farm.getSanitaryRegisterNumber() == "101"
    }

    def "should return users from farm"() {
        given:
        Farm farm1 = Mock(Farm)
        farm1.getId() >> 1
        Farm farm2 = Mock(Farm)
        farm2.getId() >> 2

        User user1 = Mock(User)
        user1.getFarm() >> farm1
        User user2 = Mock(User)
        user2.getFarm() >> farm1
        User user3 = Mock(User)
        user3.getFarm() >> farm2

        userRepository.findByFarmId(1) >> [user1, user2]
        when:
        List<User> usersInFarm1 = farmService.getUsersByFarmId(1)

        then:
        usersInFarm1.size() == 2
        usersInFarm1.contains(user1)
        usersInFarm1.contains(user2)
        !usersInFarm1.contains(user3)
        usersInFarm1.every { it.getFarm() == farm1 }
    }
}