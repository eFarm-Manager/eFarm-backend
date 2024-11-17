package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.service.agriculturalrecords.AgriculturalRecordService;
import com.efarm.efarmbackend.service.equipment.FarmEquipmentService;
import com.efarm.efarmbackend.service.finance.FinanceService;
import com.efarm.efarmbackend.service.landparcel.LandparcelService;
import com.efarm.efarmbackend.service.user.UserService;
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
    def landparcelService = Mock(LandparcelService)
    def agriculturalRecordService = Mock(AgriculturalRecordService)
    def financeService = Mock(FinanceService)
    def farmEquipmentService = Mock(FarmEquipmentService)
    def userService = Mock(UserService)
    def addressRepository = Mock(AddressRepository)

    @Subject
    FarmService farmService = new FarmService(
            farmRepository: farmRepository,
            activationCodeRepository: activationCodeRepository,
            landparcelService: landparcelService,
            agriculturalRecordService: agriculturalRecordService,
            financeService: financeService,
            farmEquipmentService: farmEquipmentService,
            userService: userService,
            addressRepository: addressRepository
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    /*
    * createFarm
    */

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

    /*
    * deactivateFarmsWithExpiredActivationCodes
    */

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

    /*
    * checkFarmDeactivation
    */

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

    /*
    * updateFarmDetails
    */

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

    /*
    * deleteInactiveFarms
    */

    def "should delete inactive farms"() {
        given:
        ActivationCode activationCode1 = Mock(ActivationCode) {
            getId() >> 1
            getExpireDate() >> LocalDate.now().minusDays(1) // <365
        }
        ActivationCode activationCode2 = Mock(ActivationCode) {
            getId() >> 2
            getExpireDate() >> LocalDate.now().minusDays(400) // >=365
        }

        Farm farm1 = Mock(Farm) {
            getId() >> 1
            getIsActive() >> false
            getIdActivationCode() >> 1
        }

        Farm farm2 = Mock(Farm) {
            getId() >> 2
            getIsActive() >> true
        }

        Farm farm3 = Mock(Farm) {
            getId() >> 3
            getIsActive() >> false
            getIdActivationCode() >> 2
        }

        farmRepository.findByIsActiveFalse() >> [farm1, farm3]

        activationCodeRepository.findById(farm1.getIdActivationCode()) >> Optional.of(activationCode1)
        activationCodeRepository.findById(farm3.getIdActivationCode()) >> Optional.of(activationCode2)

        when:
        farmService.deleteInactiveFarms()

        then:
        0 * farmRepository.delete(farm1)
        0 * farmRepository.delete(farm2)
        1 * farmRepository.delete(farm3)
    }

    /*
    * deleteFarm
    */

    def "should completely delete farm"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
            isActive >> false
            getIdAddress() >> 1
            getIdActivationCode() >> 1
        }

        when:
        farmService.deleteFarm(farm)

        then:
        1 * agriculturalRecordService.deleteAllAgriculturalRecordsForFarm(farm)
        1 * landparcelService.deleteAllLandparcelsForFarm(farm)
        1 * farmEquipmentService.deleteAllEquipmentForFarm(farm)
        1 * financeService.deleteAllTransactionsForFarm(farm)
        1 * userService.deleteAllUsersForFarm(farm)
        1 * farmRepository.delete(farm)
        1 * addressRepository.deleteById(farm.getIdAddress())
        1 * activationCodeRepository.deleteById(farm.getIdActivationCode())
    }


    /*
    * isFarmNameTaken
    */

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