package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.equipment.FarmEquipmentDTO;
import com.efarm.efarmbackend.service.equipment.FarmEquipmentFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class FarmEquipmentController {

    @Autowired
    private FarmEquipmentFacade farmEquipmentFacade;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<FarmEquipmentDTO>> getFarmEquipment(
            @RequestParam(required = false) String searchQuery
    ) {
        return farmEquipmentFacade.getFarmEquipment(searchQuery);
    }

    @GetMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> getEquipmentDetails(@PathVariable Integer equipmentId) {
        return farmEquipmentFacade.getEquipmentDetails(equipmentId);
    }
}
