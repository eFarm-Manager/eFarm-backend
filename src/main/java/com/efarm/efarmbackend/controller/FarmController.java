package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.farm.FarmDTO;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.farm.FarmFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@RequestMapping("/farm")
public class FarmController {

    private final FarmFacade farmFacade;
    private final ValidationRequestService validationRequestService;

    @GetMapping("/details")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_MANAGER')")
    public ResponseEntity<FarmDTO> getFarmDetails() {
        return ResponseEntity.ok(farmFacade.getFarmDetails());
    }

    @PutMapping("/details")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> updateFarmDetails(@Valid @RequestBody UpdateFarmDetailsRequest updateFarmDetailsRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            farmFacade.updateFarmDetails(updateFarmDetailsRequest);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie zaktualizowano dane gospodarstwa"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}