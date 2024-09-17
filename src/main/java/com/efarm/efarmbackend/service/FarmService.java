package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private UserRepository userRepository;

    public Farm createFarm(String farmName, Integer addressId, Integer activationCodeId) {
        Farm farm = new Farm(farmName);
        farm.setIdAddress(addressId);
        farm.setIdActivationCode(activationCodeId);
        farm.setIsActive(true);
        farmRepository.save(farm);
        return farm;
    }

    public List<User> getUsersByFarmId(Integer farmId) {
        return userRepository.findByFarmId(farmId);
    }
}


