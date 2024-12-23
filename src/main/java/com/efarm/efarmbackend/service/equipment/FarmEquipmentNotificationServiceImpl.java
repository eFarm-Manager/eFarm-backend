package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import com.efarm.efarmbackend.service.MainNotificationService;
import com.efarm.efarmbackend.service.user.UserManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FarmEquipmentNotificationServiceImpl implements FarmEquipmentNotificationService {

    private final FarmEquipmentRepository farmEquipmentRepository;
    private final MainNotificationService mainNotificationService;
    private final UserManagementService userManagementService;

    private static final Logger logger = LoggerFactory.getLogger(FarmEquipmentNotificationServiceImpl.class);

    @Override
    @Transactional
    public void checkInsuranceAndInspectionExpiry() {
        LocalDate today = LocalDate.now();
        List<FarmEquipment> equipments = farmEquipmentRepository.findAll();

        for (FarmEquipment equipment : equipments) {
            if (equipment.getIsAvailable()) {
                checkAndNotifyForInsurance(equipment, today);
                checkAndNotifyForInspection(equipment, today);
            }
        }
    }

    @Override
    public void checkAndNotifyForInsurance(FarmEquipment equipment, LocalDate today) {
        if (equipment.getInsuranceExpirationDate() != null) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, equipment.getInsuranceExpirationDate());

            if ((daysUntilExpiry == 14 || daysUntilExpiry == 3 || daysUntilExpiry == 1) && equipment.getFarmIdFarm().getIsActive()) {
                String message = String.format(
                        "W twoim sprzęcie %s polisa ubezpieczeniowa o numerze %s wygasa za %d dni.",
                        equipment.getEquipmentName(), equipment.getInsurancePolicyNumber(), daysUntilExpiry
                );
                List<User> owners = userManagementService.getAllOwnersForFarm(equipment.getFarmIdFarm().getId());
                for (User owner : owners) {
                    if (owner.getIsActive()) {
                        mainNotificationService.sendNotificationToUser(owner, message, "Ubezpieczenie sprzętu wygasa!");
                        logger.info("Sending insurance expire notification to owner: {}", owner.getEmail());
                    }
                }
            }
        }
    }

    @Override
    public void checkAndNotifyForInspection(FarmEquipment equipment, LocalDate today) {
        if (equipment.getInspectionExpireDate() != null) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(
                    today,
                    equipment.getInspectionExpireDate()
            );
            if ((daysUntilExpiry == 14 || daysUntilExpiry == 3 || daysUntilExpiry == 1)
                    && equipment.getFarmIdFarm().getIsActive()) {
                String message = String.format(
                        "W twoim sprzęcie %s przegląd techniczny wygasa za %d dni.",
                        equipment.getEquipmentName(), daysUntilExpiry
                );
                List<User> owners = userManagementService.getAllOwnersForFarm(equipment.getFarmIdFarm().getId());
                for (User owner : owners) {
                    mainNotificationService.sendNotificationToUser(
                            owner,
                            message,
                            "Przegląd techniczny wygasa!"
                    );
                    logger.info("Sending inspection expire notification to owner: {}", owner.getEmail());
                }
            }
        }
    }
}
