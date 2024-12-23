package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import jakarta.transaction.Transactional;

import java.time.LocalDate;

public interface FarmEquipmentNotificationService {
    @Transactional
    void checkInsuranceAndInspectionExpiry();

    void checkAndNotifyForInsurance(FarmEquipment equipment, LocalDate today);

    void checkAndNotifyForInspection(FarmEquipment equipment, LocalDate today);
}
