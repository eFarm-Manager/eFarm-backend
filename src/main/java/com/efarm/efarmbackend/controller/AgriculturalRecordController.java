package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.agriculturalrecords.AgriculturalRecordFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class AgriculturalRecordController {

    @Autowired
    private AgriculturalRecordFacade agriculturalRecordFacade;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> getFarmEquipment(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String season
    ) {
        try {
            return ResponseEntity.ok(agriculturalRecordFacade.getAgriculturalRecords(season, searchQuery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
