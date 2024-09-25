package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;

    public Address findAddressById(Integer codeId) {
        return addressRepository.findById(codeId)
                .orElseThrow(() -> new RuntimeException("Activation code not found for id: " + codeId));
    }
}
