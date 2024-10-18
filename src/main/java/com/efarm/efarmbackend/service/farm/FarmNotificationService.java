package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.service.MainNotificationService;
import com.efarm.efarmbackend.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class FarmNotificationService {

    @Autowired
    private UserService userService;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    private MainNotificationService mainNotificationService;

    private static final Logger logger = LoggerFactory.getLogger(FarmNotificationService.class);

    public void checkActivationCodeDueDateNotifications() {
        LocalDate today = LocalDate.now();

        List<Farm> farms = farmRepository.findByIsActiveTrue();
        for (Farm farm : farms) {
            checkAndNotifyForActivationCode(farm, today);
        }
    }

    private void checkAndNotifyForActivationCode(Farm farm, LocalDate today) {
        if (farm.getIdActivationCode() != null) {
            Optional<ActivationCode> currentActivationCode = activationCodeRepository.findById(farm.getIdActivationCode());
            long daysUntilExpire = ChronoUnit.DAYS.between(today, currentActivationCode.get().getExpireDate());

            if (daysUntilExpire == 14 || daysUntilExpire == 5 || daysUntilExpire == 1) {
                String message = String.format(
                        "Termin ważności kodu aktywacyjnego dla Twojej farmy %s upływa za %d dni.", farm.getFarmName(), daysUntilExpire
                );

                List<User> owners = userService.getAllOwnersForFarm(farm.getId());
                for (User owner : owners) {
                    if (owner.getIsActive()) {
                        mainNotificationService.sendNotificationToOwner(owner, message, "Niedługo wygasa kod aktywacyjny Twojej wirtualnej Farmy!");
                        logger.info("Sending expireCode info to owner: {}", owner.getEmail());
                    }
                }
            }
        }
    }
}
