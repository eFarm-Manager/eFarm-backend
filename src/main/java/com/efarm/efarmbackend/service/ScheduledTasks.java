package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.service.equipment.FarmEquipmentNotificationService;
import com.efarm.efarmbackend.service.farm.FarmNotificationService;
import com.efarm.efarmbackend.service.farm.FarmService;
import com.efarm.efarmbackend.service.finance.FinanceNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ScheduledTasks {

    private final FarmService farmService;
    private final FarmEquipmentNotificationService farmEquipmentNotificationService;
    private final FinanceNotificationService financeNotificationService;
    private final FarmNotificationService farmNotificationService;

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
        logger.info("Start checking Expire Activation Codes");
        farmNotificationService.checkActivationCodeDueDateNotifications();
        logger.info("Start checking Unpaid Financials");
        financeNotificationService.checkPaymentDueDateNotifications();
    }

    //Every monday at midnight
    @Scheduled(cron = "0 0 0 * * 1", zone = "Europe/Warsaw")
    public void checkFarmsForDelete() {
        logger.info("Start checking farms to delete");
        try {
            farmService.deleteInactiveFarms();
        } catch (Exception e) {
            logger.error("Can not delete farms with error {}", e.getMessage());
        }
    }
}