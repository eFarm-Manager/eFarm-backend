package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;

import java.util.List;

public interface ActivationCodeService {
    void validateActivationCode(String activationCode) throws RuntimeException;

    void markActivationCodeAsUsed(String activationCode);

    String generateExpireCodeInfo(Farm userFarm, List<String> roles);

    ActivationCode findActivationCodeByFarmId(Integer farmId);

    ActivationCode findActivationCodeById(Integer codeId);

    void updateActivationCodeForFarm(String newActivationCode, Integer farmId, String username);
}
