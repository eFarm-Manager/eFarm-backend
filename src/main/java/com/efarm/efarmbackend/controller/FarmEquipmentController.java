package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.payload.request.AddUpdateFarmEquipmentRequest;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentShortDTO;
import com.efarm.efarmbackend.service.equipment.FarmEquipmentFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class FarmEquipmentController {

    @Autowired
    private FarmEquipmentFacade farmEquipmentFacade;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<FarmEquipmentShortDTO>> getFarmEquipment(
            @RequestParam(required = false) String searchQuery
    ) {
        return farmEquipmentFacade.getFarmEquipment(searchQuery);
    }

    @GetMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> getEquipmentDetails(@PathVariable Integer equipmentId) {
        return farmEquipmentFacade.getEquipmentDetails(equipmentId);
    }

    @PutMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> updateFarmEquipment(@PathVariable Integer equipmentId, @Valid @RequestBody AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, BindingResult bindingResult) {
        return farmEquipmentFacade.updateFarmEquipment(equipmentId, addUpdateFarmEquipmentRequest, bindingResult);
    }

    @DeleteMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> deleteFarmEquipment(@PathVariable Integer equipmentId) {
        return farmEquipmentFacade.deleteFarmEquipment(equipmentId);
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> addNewFarmEquipment(@Valid @RequestBody AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, BindingResult bindingResult) {
        return farmEquipmentFacade.addNewFarmEquipment(addUpdateFarmEquipmentRequest, bindingResult);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<EquipmentCategoryDTO>> getAllCategoriesWithFields() {
        return farmEquipmentFacade.getEquipmentCategoriesWithFields();
    }
}
