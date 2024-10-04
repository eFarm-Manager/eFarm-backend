package com.efarm.efarmbackend.service.facades

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Address
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.farm.FarmDTO
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.UserDTO
import com.efarm.efarmbackend.payload.request.UpdateFarmDetailsRequest
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.repository.farm.AddressRepository
import com.efarm.efarmbackend.service.*
import com.efarm.efarmbackend.service.auth.AuthService
import com.efarm.efarmbackend.service.farm.ActivationCodeService
import com.efarm.efarmbackend.service.farm.AddressService
import com.efarm.efarmbackend.service.farm.FarmFacade
import com.efarm.efarmbackend.service.farm.FarmService
import com.efarm.efarmbackend.service.user.UserService
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class FarmFacadeSpec extends Specification {

    def userService = Mock(UserService)
    def farmService = Mock(FarmService)
    def activationCodeService = Mock(ActivationCodeService)
    def authService = Mock(AuthService)
    def addressService = Mock(AddressService)
    def validationRequestService = Mock(ValidationRequestService)

    @Subject
    FarmFacade farmFacade = new FarmFacade(
            userService: userService,
            farmService: farmService,
            activationCodeService: activationCodeService,
            authService: authService,
            addressService: addressService,
            validationRequestService: validationRequestService
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    def "should return farm users by farm id"() {
        given:
        Farm farm1 = Mock(Farm)
        farm1.getId() >> 1
        Farm farm2 = Mock(Farm)
        farm2.getId() >> 2

        User user1 = Mock(User)
        user1.getUsername() >> "user1"
        user1.getEmail() >> "user1@example.com"
        user1.getFirstName() >> "John"
        user1.getLastName() >> "Doe"
        user1.getPhoneNumber() >> "123456789"
        user1.getIsActive() >> true
        user1.getRole() >> Mock(Role) {
            toString() >> "ROLE_FARM_OWNER"
        }
        user1.getFarm() >> farm1
        User user2 = Mock(User)
        user2.getUsername() >> "user2"
        user2.getEmail() >> "user2@example.com"
        user2.getFirstName() >> "Jane"
        user2.getLastName() >> "Smith"
        user2.getPhoneNumber() >> ""
        user2.getIsActive() >> false
        user2.getRole() >> Mock(Role) {
            toString() >> "ROLE_FARM_EQUIPMENT_OPERATOR"
        }
        user2.getFarm() >> farm1
        User user3 = Mock(User)
        user3.getFarm() >> farm2

        userService.getLoggedUserFarm() >> farm1
        farmService.getUsersByFarmId(farm1.getId()) >> [user1, user2]

        when:
        ResponseEntity<List<UserDTO>> response = farmFacade.getFarmUsersByFarmId()

        then:
        response.getStatusCode() == HttpStatus.OK
        response.body.size() == 2
        response.body[0].username == "user1"
        response.body[1].username == "user2"
    }

    def "should return farm details"() {
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getFarmName() >> "Farm Name"
        farm.getFarmNumber() >> "123"
        farm.getFeedNumber() >> "465"
        farm.getSanitaryRegisterNumber() >> "987"
        farm.getIdActivationCode() >> 1
        Address address = Mock(Address)
        address.getStreet() >> "ulica"
        address.getBuildingNumber() >> "213D"
        address.getZipCode() >> "12-456"
        address.getCity() >> "Miasto"
        ActivationCode activationCode = Mock(ActivationCode)
        LocalDate date = LocalDate.now().plusDays(2)

        userService.getLoggedUserFarm() >> farm
        addressService.findAddressById(farm.getId()) >> address
        activationCodeService.findActivationCodeById(farm.getIdActivationCode()) >> activationCode
        authService.hasCurrentUserRole("ROLE_FARM_OWNER") >> true
        activationCode.getExpireDate() >> date

        when:
        ResponseEntity<FarmDTO> response = farmFacade.getFarmDetails()

        then:
        response.getStatusCode() == HttpStatus.OK
        response.body.farmName == "Farm Name"
        response.body.street == "ulica"
        response.body.activationCodeExpireDate == date
    }

    def "should update farm details correctly"() {
        given:
        UpdateFarmDetailsRequest updateFarmDetailsRequest = new UpdateFarmDetailsRequest(
                farmName: "New Farm",
                farmNumber: "202",
                feedNumber: "456",
                sanitaryRegisterNumber: "101",
                street: "ulica",
                buildingNumber: "20",
                zipCode: "05-132",
                city: "Miasto"
        )
        Farm farm = new Farm()
        farm.setId(1)
        farm.setIdAddress(1)
        farm.setFarmName("Old Farm")
        farm.setFarmNumber("123")
        farm.setFeedNumber("456")
        farm.setSanitaryRegisterNumber("987")
        Address address = new Address()
        address.setId(1)
        address.setStreet("nie ulica")
        address.setBuildingNumber("1")
        address.setZipCode("05-132")
        address.setCity("nie miasto")
        BindingResult bindingResult = Mock(BindingResult)
        FarmRepository farmRepository = Mock(FarmRepository)
        AddressRepository addressRepository = Mock(AddressRepository)

        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userService.getLoggedUserFarm() >> farm
        farmService.updateFarmDetails(farm, updateFarmDetailsRequest) >> {
            farm.setFarmName(updateFarmDetailsRequest.getFarmName())
        }
        farmRepository.save(farm) >> farm
        addressService.findAddressById(address.getId()) >> address
        addressService.updateFarmAddress(address, updateFarmDetailsRequest) >> {
            address.setStreet(updateFarmDetailsRequest.getStreet())
        }
        addressRepository.save(address) >> address

        when:
        ResponseEntity<?> response = farmFacade.updateFarmDetails(updateFarmDetailsRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.OK
        response.body.message == "Poprawnie zaktualizowamo dane gospodarstwa"
        farm.getFarmName() == "New Farm"
        address.getStreet() == "ulica"
    }

}