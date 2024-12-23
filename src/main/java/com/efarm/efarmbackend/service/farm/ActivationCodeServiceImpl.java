package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.exception.TooManyRequestsException;
import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.security.services.BruteForceProtectionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ActivationCodeServiceImpl implements ActivationCodeService {

    private final ActivationCodeRepository activationCodeRepository;
    private final FarmRepository farmRepository;
    private final BruteForceProtectionService bruteForceProtectionService;

    @Value("${efarm.app.notification.daysToShowExpireActivationCode}")
    private int daysToShowExpireActivationCodeNotification;

    private static final Logger logger = LoggerFactory.getLogger(ActivationCodeServiceImpl.class);

    @Override
    public void validateActivationCode(String activationCode) throws RuntimeException {
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(activationCode);
        if (activationCodeOpt.isEmpty()) {
            throw new RuntimeException("Podany kod aktywacyjny nie istnieje");
        }

        if (activationCodeOpt.get().getExpireDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Kod aktywacyjny wygasł");
        }

        if (activationCodeOpt.get().getIsUsed()) {
            throw new RuntimeException("Podany kod aktywacyjny został już wykorzystany");
        }
    }

    @Override
    public void markActivationCodeAsUsed(String activationCode) {
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(activationCode);
        if (activationCodeOpt.isPresent()) {
            ActivationCode activationCodeEntity = activationCodeOpt.get();
            activationCodeEntity.setIsUsed(true);
            activationCodeRepository.save(activationCodeEntity);
        } else {
            logger.warn("Can not user activation code: {}", activationCode);
            throw new RuntimeException("Activation code not found");
        }
    }

    @Override
    public String generateExpireCodeInfo(Farm userFarm, List<String> roles) {
        if (roles.contains("ROLE_FARM_OWNER")) {
            ActivationCode activationCode = findActivationCodeByFarmId(userFarm.getId());
            long daysToExpiration = ChronoUnit.DAYS.between(LocalDate.now(), activationCode.getExpireDate());

            if (daysToExpiration <= daysToShowExpireActivationCodeNotification && daysToExpiration >= 0) {
                return "Kod aktywacyjny wygasa za " + daysToExpiration + " dni";
            }
        }
        return null;
    }

    @Override
    public ActivationCode findActivationCodeByFarmId(Integer farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farma o ID " + farmId + " nie została znaleziona"));

        Integer activationCodeId = farm.getIdActivationCode();

        return activationCodeRepository.findById(activationCodeId)
                .orElseThrow(() -> new RuntimeException("Kod aktywacyjny o id" + activationCodeId + " nie został znaleziony"));
    }

    @Override
    public ActivationCode findActivationCodeById(Integer codeId) {
        return activationCodeRepository.findById(codeId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono kodu aktywacyjnego o id" + codeId));
    }

    @Override
    public void updateActivationCodeForFarm(String newActivationCode, Integer farmId, String username) {

        if (bruteForceProtectionService.isBlocked(username)) {
            throw new TooManyRequestsException("Zbyt wiele nieudanych prób logowania! Spróbuj ponownie później");
        }

        validateActivationCode(newActivationCode);
        bruteForceProtectionService.loginSucceeded(username);
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(newActivationCode);
        ActivationCode newActivationCodeEntity = activationCodeOpt.get();

        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono kodu aktywacyjnego o id: " + farmId));

        Optional<ActivationCode> currentActivationCodeOpt = activationCodeRepository.findById(farm.getIdActivationCode());
        if (currentActivationCodeOpt.isPresent()) {
            ActivationCode currentActivationCodeEntity = currentActivationCodeOpt.get();
            activationCodeRepository.delete(currentActivationCodeEntity);
        }

        farm.setIdActivationCode(newActivationCodeEntity.getId());
        farm.setIsActive(true);
        farmRepository.save(farm);
        newActivationCodeEntity.setIsUsed(true);
        activationCodeRepository.save(newActivationCodeEntity);
    }
}