package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.repository.user.UserRepository
import com.efarm.efarmbackend.service.farm.FarmService
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import spock.lang.Subject
import java.nio.file.AccessDeniedException

import java.time.LocalDate

class FarmServiceSpec extends Specification {

    def farmRepository = Mock(FarmRepository)
    def activationCodeRepository = Mock(ActivationCodeRepository)
    def userRepository = Mock(UserRepository)

    @Subject
    FarmService farmService = new FarmService(
            farmRepository: farmRepository,
            activationCodeRepository: activationCodeRepository,
            userRepository: userRepository
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
        farmService.checkFarmDeactivation(userFarm, role_owner)

        then:
        AccessDeniedException ex = thrown()
        ex.message == "Gospodarstwo jest nieaktywne. Podaj nowy kod aktywacyjny."
    }

    def "should show that farm not active for manager"() {
        given:
        Role role_manager = Mock(Role)
        role_manager.getName() >> ERole.ROLE_FARM_MANAGER
        Farm userFarm = Mock(Farm)
        userFarm.getId() >> 1
        userFarm.getIsActive() >> false

        when:
        farmService.checkFarmDeactivation(userFarm, role_manager)

        then:
	AccessDeniedException ex = thrown()
	ex.message == "Gospodarstwo jest nieaktywne. Kod aktywacyjny wygasł." 
    }

    def "should show that farm not active for operator"() {
        given:
        Role role_operator = Mock(Role)
        role_operator.getName() >> ERole.ROLE_FARM_EQUIPMENT_OPERATOR
        Farm userFarm = Mock(Farm)
        userFarm.getId() >> 1
        userFarm.getIsActive() >> false

        when:
        farmService.checkFarmDeactivation(userFarm, role_operator)

        then:
	AccessDeniedException ex = thrown()
	ex.message == "Gospodarstwo jest nieaktywne. Kod aktywacyjny wygasł."
    }

    def "should not show inactive message because farm is active"() {
        given:
        Role role_owner = Mock(Role)
        role_owner.getName() >> ERole.ROLE_FARM_OWNER
        Farm userFarm = Mock(Farm)
        userFarm.getId() >> 1
        userFarm.getIsActive() >> true

        when:
        farmService.checkFarmDeactivation(userFarm, role_owner)

        then:
        noExceptionThrown()
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
        farmRepository.existsByFarmName(updateFarmDetailsRequest.getFarmName()) >> false

        when:
        farmService.updateFarmDetails(farm, updateFarmDetailsRequest)

        then:
        1 * farmRepository.save(farm)
        farm.getFarmName() == "New Farm"
        farm.getFarmNumber() == "202"
        farm.getFeedNumber() == "456"
        farm.getSanitaryRegisterNumber() == "101"
    }

    def "should update when farm name doesnt change"() {
        given:
        Farm farm = new Farm()
        farm.setId(1)
        farm.setFarmName("Old Farm")
        farm.setFarmNumber("123")
        farm.setFeedNumber("456")
        farm.setSanitaryRegisterNumber("987")
        UpdateFarmDetailsRequest updateFarmDetailsRequest = new UpdateFarmDetailsRequest(
                farmName: "Old Farm",
                farmNumber: "202",
                feedNumber: "456",
                sanitaryRegisterNumber: "101"
        )

        farmRepository.existsByFarmName(updateFarmDetailsRequest.getFarmName()) >> true

        when:
        farmService.updateFarmDetails(farm, updateFarmDetailsRequest)

        then:
        1 * farmRepository.save(farm)
        farm.getFarmName() == "Old Farm"
        farm.getFarmNumber() == "202"
        farm.getFeedNumber() == "456"
        farm.getSanitaryRegisterNumber() == "101"
    }

    def "should not update farm details when farm name is already taken"() {
        given:
        Farm existingFarm = new Farm()
        existingFarm.setId(1)
        existingFarm.setFarmName("New Farm")
        
        Farm farm = new Farm()
        farm.setId(2)
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
        farmRepository.existsByFarmName(updateFarmDetailsRequest.getFarmName()) >> true

        when:
        farmService.updateFarmDetails(farm, updateFarmDetailsRequest)

        then:
        0 * farmRepository.save(farm)
        IllegalArgumentException ex = thrown()
        ex.message == "Wybrana nazwa farmy jest zajęta. Spróbuj wybrać inną"
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

    def "should return true if farm exists by name"() {
        given:
        String farmName = "Farm Name"
        farmRepository.existsByFarmName(farmName) >> true

        when:
        boolean result = farmService.isFarmNameTaken(farmName)

        then:
        result == true
    }

    def "should return false if farm does not exist by name"() {
        given:
        String farmName = "Farm Name"
        farmRepository.existsByFarmName(farmName) >> false

        when:
        boolean result = farmService.isFarmNameTaken(farmName)

        then:
        result == false
    }
}