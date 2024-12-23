package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.service.MainNotificationService;
import com.efarm.efarmbackend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FarmNotificationServiceImpl implements FarmNotificationService {

    private final UserService userService;
    private final FarmRepository farmRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final MainNotificationService mainNotificationService;

    private static final Logger logger = LoggerFactory.getLogger(FarmNotificationServiceImpl.class);

    @Override
    public void checkActivationCodeDueDateNotifications() {
        LocalDate today = LocalDate.now();
        List<Farm> farms = farmRepository.findAll();
        for (Farm farm : farms) {
            if (farm.getIsActive()) {
                checkAndNotifyForActivationCode(farm, today);
            } else {
                checkAndNotifyForFarmDeletion(farm, today);
            }
        }
    }

    @Override
    public void checkAndNotifyForActivationCode(Farm farm, LocalDate today) {
        if (farm.getIdActivationCode() != null) {
            Optional<ActivationCode> currentActivationCode = activationCodeRepository.findById(farm.getIdActivationCode());
            if (currentActivationCode.isPresent()) {
                long daysUntilExpire = ChronoUnit.DAYS.between(today, currentActivationCode.get().getExpireDate());

                if (daysUntilExpire == 14 || daysUntilExpire == 5 || daysUntilExpire == 1) {
                    String message = String.format(
                            "Termin ważności kodu aktywacyjnego dla Twojej farmy %s upływa za %d dni.", farm.getFarmName(), daysUntilExpire
                    );

                    List<User> owners = userService.getAllOwnersForFarm(farm.getId());
                    for (User owner : owners) {
                        if (owner.getIsActive()) {
                            mainNotificationService.sendNotificationToUser(owner, message, "Niedługo wygasa kod aktywacyjny Twojej wirtualnej Farmy!");
                            logger.info("Sending expireCode info to owner: {}", owner.getEmail());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void checkAndNotifyForFarmDeletion(Farm farm, LocalDate today) {
        if (farm.getIdActivationCode() != null) {
            Optional<ActivationCode> currentActivationCode = activationCodeRepository.findById(farm.getIdActivationCode());
            if (currentActivationCode.isPresent()) {
                long daysSinceExpiration = ChronoUnit.DAYS.between(currentActivationCode.get().getExpireDate(), today);
                long daysUntilDeletion = 365 - daysSinceExpiration;
                if (daysUntilDeletion == 3 || daysUntilDeletion == 2 || daysUntilDeletion == 1) {
                    String message = String.format(
                            "Twoja farma %s zostanie trwale usunięta za %d dni. Zaktualizuj swój kod aktywacyjny, aby temu zapobiec.",
                            farm.getFarmName(), daysUntilDeletion
                    );

                    List<User> owners = userService.getAllOwnersForFarm(farm.getId());
                    for (User owner : owners) {
                        if (owner.getIsActive()) {
                            mainNotificationService.sendNotificationToUser(owner, message, "Twoja farma zostanie wkrótce usunięta!"
                            );
                            logger.info("Notification about farm deletion has been sent to the owner:: {}", owner.getEmail());
                        }
                    }
                }
            }
        }
    }
}