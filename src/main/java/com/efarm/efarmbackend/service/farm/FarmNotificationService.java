package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.Farm;

import java.time.LocalDate;

public interface FarmNotificationService {
    void checkActivationCodeDueDateNotifications();

    void checkAndNotifyForActivationCode(Farm farm, LocalDate today);

    void checkAndNotifyForFarmDeletion(Farm farm, LocalDate today);
}
