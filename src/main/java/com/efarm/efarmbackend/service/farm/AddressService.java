package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;

    public Address findAddressById(Integer codeId) {
        return addressRepository.findById(codeId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono kodu aktywacyjnego o id: " + codeId));
    }

    public void updateFarmAddress(Address address, UpdateFarmDetailsRequest updateFarmDetailsRequest) {
        if (updateFarmDetailsRequest.getStreet() != null) {
            address.setStreet(updateFarmDetailsRequest.getStreet());
        }
        if (updateFarmDetailsRequest.getBuildingNumber() != null) {
            address.setBuildingNumber(updateFarmDetailsRequest.getBuildingNumber());
        }
        if (updateFarmDetailsRequest.getZipCode() != null) {
            address.setZipCode(updateFarmDetailsRequest.getZipCode());
        }
        if (updateFarmDetailsRequest.getCity() != null) {
            address.setCity(updateFarmDetailsRequest.getCity());
        }
        addressRepository.save(address);
    }
}
