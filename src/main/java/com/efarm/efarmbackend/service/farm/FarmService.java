package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
import jakarta.transaction.Transactional;

public interface FarmService {
    Farm createFarm(String farmName, Integer addressId, Integer activationCodeId);

    void deactivateFarmsWithExpiredActivationCodes();

    void checkFarmDeactivation(Farm userFarm, Role role) throws Exception;

    void updateFarmDetails(Farm loggedUserFarm, UpdateFarmDetailsRequest request);

    @Transactional
    void deleteInactiveFarms() throws Exception;

    @Transactional
    void deleteFarm(Farm farm) throws Exception;

    Boolean isFarmNameTaken(String name);
}
