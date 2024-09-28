package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.payload.request.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spock.lang.Specification
import spock.lang.Subject

class AddressServiceSpec extends Specification {

    def addressRepository = Mock(AddressRepository)

    @Subject
    AddressService addressService = new AddressService(
            addressRepository: addressRepository
    )

    def "should find address by its id"() {
        given:
        Address address = Mock(Address)
        address.getId() >> 1
        addressRepository.findById(1) >> Optional.of(address)

        when:
        Address foundAddress = addressService.findAddressById(1)

        then:
        address == foundAddress
    }

    def "should not find address that doesnt exist by its id"() {
        given:
        addressRepository.findById(1) >> Optional.empty()

        when:
        Address foundAddress = addressService.findAddressById(1)

        then:
        thrown(RuntimeException)
    }

    def "should update address details - street, building number and city"() {
        given:
        Address address = new Address()
        address.setId(1)
        address.setStreet("nie ulica") 
        address.setBuildingNumber("1") 
        address.setZipCode("05-132") 
        address.setCity("nie miasto") 
        UpdateFarmDetailsRequest updateFarmDetailsRequest = new UpdateFarmDetailsRequest(
            street: "ulica", 
            buildingNumber: "20", 
            zipCode: "05-132", 
            city: "Miasto"
        )

        when:
        addressService.updateFarmAddress(address,updateFarmDetailsRequest)

        then:
        1 * addressRepository.save(address)
        address.getStreet() == "ulica"
        address.getBuildingNumber() == "20"
        address.getZipCode() == "05-132"
        address.getCity() == "Miasto"
    }



}