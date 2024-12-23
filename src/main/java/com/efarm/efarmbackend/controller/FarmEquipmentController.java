package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.equipment.FarmEquipmentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@RequestMapping("/equipment")
public class FarmEquipmentController {

    private final FarmEquipmentFacade farmEquipmentFacade;
    private final ValidationRequestService validationRequestService;

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
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> updateFarmEquipment(@PathVariable Integer equipmentId, @Valid @RequestBody AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            farmEquipmentFacade.updateFarmEquipment(equipmentId, addUpdateFarmEquipmentRequest);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie zaktualizowane dane maszyny"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{equipmentId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> deleteFarmEquipment(@PathVariable Integer equipmentId) {
        try {
            farmEquipmentFacade.deleteFarmEquipment(equipmentId);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie usunięto maszynę"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> addNewFarmEquipment(
            @Valid @RequestBody AddUpdateFarmEquipmentRequest request,
            BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            farmEquipmentFacade.addNewFarmEquipment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Pomyślnie dodano nową maszynę"));
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
