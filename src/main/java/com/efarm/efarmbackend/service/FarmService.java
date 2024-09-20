package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
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

    @Value("${efarm.app.frontend.updateActivationCodeUri}")
    private String frontendUriToUpdateActivationCode;

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

    public ResponseEntity<?> checkFarmDeactivation(Farm userFarm, Role role) {
        if (!userFarm.getIsActive()) {
            if (role.getName() == ERole.ROLE_FARM_EQUIPMENT_OPERATOR ||
                    role.getName() == ERole.ROLE_FARM_MANAGER) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Gospodarstwo jest nieaktywne."));
            }

            if (role.getName() == ERole.ROLE_FARM_OWNER) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .location(URI.create(frontendUriToUpdateActivationCode))
                        .body(new MessageResponse("Gospodarstwo jest nieaktywne. Podaj nowy kod aktywacyjny."));
            }
        }
        return null;
    }

    public List<User> getUsersByFarmId(Integer farmId) {
        return userRepository.findByFarmId(farmId);
    }
}


