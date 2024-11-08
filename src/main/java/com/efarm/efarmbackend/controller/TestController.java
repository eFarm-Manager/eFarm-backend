package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.equipment.FarmEquipmentNotificationService;
import com.efarm.efarmbackend.service.finance.FinanceNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private FarmEquipmentNotificationService farmEquipmentNotificationService;

    @Autowired
    private FinanceNotificationService financeNotificationService;

    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("/operator")
    @PreAuthorize("hasRole('ROLE_FARM_EQUIPMENT_OPERATOR')")
    public String userAccess() {
        return "Operator Board.";
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER')")
    public String adminAccess() {
        return "Manager Board.";
    }

    @GetMapping("/owner")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public String wonerAccess() {
        return "Owner Board.";
    }

    @GetMapping("/runInsuranceCronJob")
    public ResponseEntity<?> runCronJobManually() {
        farmEquipmentNotificationService.checkInsuranceAndInspectionExpiry();
        return ResponseEntity.ok().body(new MessageResponse("Manual CRON job executed"));
    }

    @GetMapping("/runFinanceCronJob")
    public ResponseEntity<?> runFinanceJobManually() {
        financeNotificationService.checkPaymentDueDateNotifications();
        return ResponseEntity.ok().body(new MessageResponse("Manual CRON job executed"));
    }
}
