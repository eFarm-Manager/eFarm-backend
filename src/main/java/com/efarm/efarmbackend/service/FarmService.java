package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(FarmService.class);

    public Farm createFarm(String farmName, Integer addressId, Integer activationCodeId) {
        Farm farm = new Farm(farmName);
        farm.setIdAddress(addressId);
        farm.setIdActivationCode(activationCodeId);
        farm.setIsActive(true);
        farmRepository.save(farm);
        return farm;
    }

    public void deactivateFarmsWithExpiredActivationCodes() {
        List<Farm> activeFarms = farmRepository.findByIsActiveTrue();

        for (Farm farm : activeFarms) {
            ActivationCode activationCode = activationCodeRepository.findById(farm.getIdActivationCode())
                    .orElseThrow(() -> new RuntimeException("Activation code not found for farm: " + farm.getId()));

            if (activationCode.getExpireDate().isBefore(LocalDate.now())) {
                logger.info("Deactivating farm: {}", farm.getId());
                farm.setIsActive(false);
                farmRepository.save(farm);
            }
        }
    }

    public List<User> getUsersByFarmId(Integer farmId) {
        return userRepository.findByFarmId(farmId);
    }
}


