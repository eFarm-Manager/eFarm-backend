package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.service.equipment.FarmEquipmentNotificationService;
import com.efarm.efarmbackend.service.farm.FarmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTasks {

    @Autowired
    private FarmService farmService;

    @Autowired
    private FarmEquipmentNotificationService farmEquipmentNotificationService;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    //Every day at midnight
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Warsaw")
    public void checkFarmsForExpiredActivationCodes() {
        logger.info("Start checking expired Activation Codes");
        try {
            farmService.deactivateFarmsWithExpiredActivationCodes();
        } catch (RuntimeException e) {
            logger.error("Can not deactivate farms with error {}", e.getMessage());
        }
    }

    //Every day at 7:00
    @Scheduled(cron = "0 0 7 * * *", zone = "Europe/Warsaw")
    public void checkInsuranceAndInspectionExpiry() {
        logger.info("Start checking Insurance and Inspection Expiry");
        farmEquipmentNotificationService.checkInsuranceAndInspectionExpiry();
    }
}
