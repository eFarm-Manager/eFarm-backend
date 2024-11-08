package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
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

    public void checkFarmDeactivation(Farm userFarm, Role role) throws Exception {
        if (!userFarm.getIsActive()) {
            if (role.getName() == ERole.ROLE_FARM_EQUIPMENT_OPERATOR ||
                    role.getName() == ERole.ROLE_FARM_MANAGER) {
                throw new AccessDeniedException("Gospodarstwo jest nieaktywne. Kod aktywacyjny wygasł.");
            }
            if (role.getName() == ERole.ROLE_FARM_OWNER) {
                throw new AccessDeniedException("Gospodarstwo jest nieaktywne. Podaj nowy kod aktywacyjny.");
            }
        }
    }

    public void updateFarmDetails(Farm loggedUserFarm, UpdateFarmDetailsRequest request) {
        if (!loggedUserFarm.getFarmName().equals(request.getFarmName()) &&
                isFarmNameTaken(request.getFarmName())
        ) {
            throw new IllegalArgumentException("Wybrana nazwa farmy jest zajęta. Spróbuj wybrać inną");
        } else if (request.getFarmName() != null) {
            loggedUserFarm.setFarmName(request.getFarmName());
        }
        if (request.getFarmNumber() != null) {
            loggedUserFarm.setFarmNumber(request.getFarmNumber());
        }
        if (request.getFeedNumber() != null) {
            loggedUserFarm.setFeedNumber(request.getFeedNumber());
        }
        if (request.getSanitaryRegisterNumber() != null) {
            loggedUserFarm.setSanitaryRegisterNumber(request.getSanitaryRegisterNumber());
        }
        farmRepository.save(loggedUserFarm);
    }

    public List<User> getUsersByFarmId(Integer farmId) {
        return userRepository.findByFarmId(farmId);
    }

    private Boolean isFarmNameTaken(String name) {
        return farmRepository.existsByFarmName(name);
    }
}