package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.farm.FarmDTO;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.farm.FarmFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/farm")
public class FarmController {

    @Autowired
    private FarmFacade farmFacade;

    @Autowired
    private ValidationRequestService validationRequestService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<UserDTO>> getFarmUsersByFarmId() {
        return ResponseEntity.ok(farmFacade.getFarmUsersByFarmId());
    }

    @GetMapping("/details")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_MANAGER')")
    public ResponseEntity<FarmDTO> getFarmDetails() {
        return ResponseEntity.ok(farmFacade.getFarmDetails());
    }

    @PutMapping("/details")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> updateFarmDetails(@Valid @RequestBody UpdateFarmDetailsRequest updateFarmDetailsRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            return ResponseEntity.ok(farmFacade.updateFarmDetails(updateFarmDetailsRequest));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}