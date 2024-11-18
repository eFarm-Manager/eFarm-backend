package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.service.agriculturalrecords.AgriculturalRecordService;
import com.efarm.efarmbackend.service.equipment.FarmEquipmentService;
import com.efarm.efarmbackend.service.finance.FinanceService;
import com.efarm.efarmbackend.service.landparcel.LandparcelService;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;


@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    private LandparcelService landparcelService;

    @Autowired
    private AgriculturalRecordService agriculturalRecordService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private FarmEquipmentService farmEquipmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressRepository addressRepository;

    private static final Logger logger = LoggerFactory.getLogger(FarmService.class);

    public Farm createFarm(String farmName, Integer addressId, Integer activationCodeId) {
        Farm farm = new Farm(farmName, addressId, activationCodeId, true);
        farmRepository.save(farm);
        return farm;
    }

    public void deactivateFarmsWithExpiredActivationCodes() {
        List<Farm> activeFarms = farmRepository.findByIsActiveTrue();

        for (Farm farm : activeFarms) {
            ActivationCode activationCode = activationCodeRepository.findById(farm.getIdActivationCode())
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono kodu aktywacyjnego dla farmy o id: " + farm.getId()));

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
            throw new IllegalArgumentException("Wybrana nazwa farmy jest zajęta. Spróbuj wybrać inną.");
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

    @Transactional
    public void deleteInactiveFarms() throws Exception {
        List<Farm> inactiveFarms = farmRepository.findByIsActiveFalse();
        LocalDate today = LocalDate.now();
        for (Farm farm : inactiveFarms) {
            Optional<ActivationCode> currentActivationCode = activationCodeRepository.findById(farm.getIdActivationCode());
            if (currentActivationCode.isPresent()) {
                long daysSinceExpiration = ChronoUnit.DAYS.between(currentActivationCode.get().getExpireDate(), today);
                if (daysSinceExpiration >= 365) {
                    deleteFarm(farm);
                }
            }
        }
    }


    @Transactional
    public void deleteFarm(Farm farm) throws Exception {
        if (farm != null && !farm.getIsActive()) {
            agriculturalRecordService.deleteAllAgriculturalRecordsForFarm(farm);
            landparcelService.deleteAllLandparcelsForFarm(farm);
            farmEquipmentService.deleteAllEquipmentForFarm(farm);
            financeService.deleteAllTransactionsForFarm(farm);
            userService.deleteAllUsersForFarm(farm);
            Integer farmId = farm.getId();
            Integer idAddress = farm.getIdAddress();
            Integer idActivationCode = farm.getIdActivationCode();
            farmRepository.delete(farm);
            addressRepository.deleteById(idAddress);
            activationCodeRepository.deleteById(idActivationCode);
            logger.info("Deleted Farm: {}", farmId);
        }
    }

    private Boolean isFarmNameTaken(String name) {
        return farmRepository.existsByFarmName(name);
    }
}