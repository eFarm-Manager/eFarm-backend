package com.efarm.efarmbackend.service.facades

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Address
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.farm.FarmDTO
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest
import com.efarm.efarmbackend.repository.farm.AddressRepository
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.service.auth.AuthService
import com.efarm.efarmbackend.service.farm.ActivationCodeService
import com.efarm.efarmbackend.service.farm.AddressService
import com.efarm.efarmbackend.service.farm.FarmFacade
import com.efarm.efarmbackend.service.farm.FarmService
import com.efarm.efarmbackend.service.user.UserService
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class FarmFacadeSpec extends Specification {

    def userService = Mock(UserService)
    def farmService = Mock(FarmService)
    def activationCodeService = Mock(ActivationCodeService)
    def authService = Mock(AuthService)
    def addressService = Mock(AddressService)

    @Subject
    FarmFacade farmFacade = new FarmFacade(
            userService,
            farmService,
            activationCodeService,
            authService,
            addressService
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }


    def "should return farm details"() {
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getFarmName() >> 'Farm Name'
        farm.getFarmNumber() >> '123'
        farm.getFeedNumber() >> '465'
        farm.getSanitaryRegisterNumber() >> '987'
        farm.getIdActivationCode() >> 1
        Address address = Mock(Address)
        address.getStreet() >> 'ulica'
        address.getBuildingNumber() >> '213D'
        address.getZipCode() >> '12-456'
        address.getCity() >> 'Miasto'
        ActivationCode activationCode = Mock(ActivationCode)
        LocalDate date = LocalDate.now().plusDays(2)

        userService.getLoggedUserFarm() >> farm
        addressService.findAddressById(farm.getId()) >> address
        activationCodeService.findActivationCodeById(farm.getIdActivationCode()) >> activationCode
        authService.hasCurrentUserRole('ROLE_FARM_OWNER') >> true
        activationCode.getExpireDate() >> date

        when:
        FarmDTO response = farmFacade.getFarmDetails()

        then:
        response.farmName == 'Farm Name'
        response.street == 'ulica'
        response.activationCodeExpireDate == date
    }

    def "should update farm details correctly"() {
        given:
        UpdateFarmDetailsRequest updateFarmDetailsRequest = new UpdateFarmDetailsRequest(
                farmName: 'New Farm',
                farmNumber: '202',
                feedNumber: '456',
                sanitaryRegisterNumber: '101',
                street: 'ulica',
                buildingNumber: '20',
                zipCode: '05-132',
                city: 'Miasto'
        )
        Farm farm = new Farm()
        farm.setId(1)
        farm.setIdAddress(1)
        farm.setFarmName('Old Farm')
        farm.setFarmNumber('123')
        farm.setFeedNumber('456')
        farm.setSanitaryRegisterNumber('987')
        Address address = new Address()
        address.setId(1)
        address.setStreet('nie ulica')
        address.setBuildingNumber('1')
        address.setZipCode('05-132')
        address.setCity('nie miasto')
        FarmRepository farmRepository = Mock(FarmRepository)
        AddressRepository addressRepository = Mock(AddressRepository)

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
        farmFacade.updateFarmDetails(updateFarmDetailsRequest)

        then:
        farm.getFarmName() == 'New Farm'
        address.getStreet() == 'ulica'
    }

}
