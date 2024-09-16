package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class FarmService {
    @Autowired
    private FarmRepository farmRepository;

    public Farm createFarm(String farmName, Integer addressId, Integer activationCodeId) {
        Farm farm = new Farm(farmName);
        farm.setIdAddress(addressId);
        farm.setIdActivationCode(activationCodeId);
        farm.setIsActive(true);
        farmRepository.save(farm);
        return farm;
    }
}


