package com.efarm.efarmbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTasks {

    @Autowired
    private FarmService farmService;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    //Every day at midnight
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Warsaw")
    public void checkFarmsForExpiredActivationCodes() {
        logger.info("Start checking expired Activation Codes");
        try {
            farmService.deactivateFarmsWithExpiredActivationCodes();
        } catch (RuntimeException e) {
            logger.error("Can not deactivate farms with error {}",  e.getMessage());
        }
    }
}
