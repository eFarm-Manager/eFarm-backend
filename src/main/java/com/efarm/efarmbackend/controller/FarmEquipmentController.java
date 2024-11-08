package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.equipment.FarmEquipmentFacade;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private UserService userService;

    @Autowired
    private ValidationRequestService validationRequestService;

    private static final Logger logger = LoggerFactory.getLogger(FarmEquipmentController.class);

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<EquipmentSummaryDTO>> getFarmEquipment(
            @RequestParam(required = false) String searchQuery
    ) {
        return ResponseEntity.ok(farmEquipmentFacade.getFarmEquipment(searchQuery));
    }

    @GetMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> getEquipmentDetails(@PathVariable Integer equipmentId) {
        try {
            return ResponseEntity.ok(farmEquipmentFacade.getEquipmentDetails(equipmentId));
        } catch (Exception e) {
            User user = userService.getLoggedUser();
            logger.info("Equipment with id: {}, not found for user: {}", equipmentId, user.getUsername());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> updateFarmEquipment(@PathVariable Integer equipmentId, @Valid @RequestBody AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            return ResponseEntity.ok(farmEquipmentFacade.updateFarmEquipment(equipmentId, addUpdateFarmEquipmentRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> deleteFarmEquipment(@PathVariable Integer equipmentId) {
        try {
            return ResponseEntity.ok(farmEquipmentFacade.deleteFarmEquipment(equipmentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> addNewFarmEquipment(@Valid @RequestBody AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(farmEquipmentFacade.addNewFarmEquipment(addUpdateFarmEquipmentRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<EquipmentCategoryDTO>> getAllCategoriesWithFields() {
        return ResponseEntity.ok(farmEquipmentFacade.getEquipmentCategoriesWithFields());
    }
}
