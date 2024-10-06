package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import com.efarm.efarmbackend.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FarmEquipmentNotificationService {

    @Autowired
    private FarmEquipmentRepository farmEquipmentRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(FarmEquipmentNotificationService.class);

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

    private void sendNotificationToOwner(User owner, String message, String subject) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(owner.getEmail());
        email.setSubject(subject);
        email.setText(message);

        mailSender.send(email);
    }

    private void checkAndNotifyForInsurance(FarmEquipment equipment, LocalDate today) {
        if (equipment.getInsuranceExpirationDate() != null) {
            long daysUntilExpiry = today.until(equipment.getInsuranceExpirationDate()).getDays();

            if (daysUntilExpiry == 14 || daysUntilExpiry == 3 || daysUntilExpiry == 1) {
                String message = String.format(
                        "W twoim sprzęcie %s polisa ubezpieczeniowa o numerze %s wygasa za %d dni.",
                        equipment.getEquipmentName(), equipment.getInsurancePolicyNumber(), daysUntilExpiry
                );
                List<User> owners = userService.getAllOwnersForFarm(equipment.getFarmIdFarm().getId());
                for (User owner : owners) {
                    sendNotificationToOwner(owner, message, "Ubezpieczenie sprzętu wygasa!");
                    logger.info("Sending insurance expire notification to owner: {}", owner.getEmail());
                }
            }
        }
    }

    private void checkAndNotifyForInspection(FarmEquipment equipment, LocalDate today) {
        if (equipment.getInspectionExpireDate() != null) {
            long daysUntilExpiry = today.until(equipment.getInspectionExpireDate()).getDays();

            if (daysUntilExpiry == 14 || daysUntilExpiry == 3 || daysUntilExpiry == 1) {
                String message = String.format(
                        "W twoim sprzęcie %s przegląd techniczny wygasa za %d dni.",
                        equipment.getEquipmentName(), daysUntilExpiry
                );
                List<User> owners = userService.getAllOwnersForFarm(equipment.getFarmIdFarm().getId());
                for (User owner : owners) {
                    sendNotificationToOwner(owner, message, "Przegląd techniczny wygasa!");
                    logger.info("Sending inspection expire notification to owner: {}", owner.getEmail());
                }
            }
        }
    }
}
