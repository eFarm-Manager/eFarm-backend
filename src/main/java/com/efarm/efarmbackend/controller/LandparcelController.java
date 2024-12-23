package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import com.efarm.efarmbackend.payload.request.landparcel.AddLandparcelRequest;
import com.efarm.efarmbackend.payload.request.landparcel.UpdateLandparcelRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.landparcel.LandparcelFacade;
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
@RequestMapping("/landparcel")
public class LandparcelController {

    private final LandparcelFacade landparcelFacade;
    private final ValidationRequestService validationRequestService;

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> addNewLandparcel(@Valid @RequestBody AddLandparcelRequest addLandparcelRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            landparcelFacade.addNewLandparcel(addLandparcelRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Pomyślnie dodano nową działkę"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{landparcelId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> getLandparcelDetails(@PathVariable Integer landparcelId) {
        try {
            LandparcelDTO landparcelDTO = landparcelFacade.getLandparcelDetails(landparcelId);
            return ResponseEntity.ok(landparcelDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{landparcelId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> updateLandparcel(@PathVariable Integer landparcelId, @Valid @RequestBody UpdateLandparcelRequest updateLandparcelRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            landparcelFacade.updateLandparcel(landparcelId, updateLandparcelRequest);
            return ResponseEntity.ok(new MessageResponse("Dane działki zostały pomyślnie zaktualizowane"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{landparcelId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> deleteLandparcel(@PathVariable Integer landparcelId) {
        try {
            landparcelFacade.deleteLandparcel(landparcelId);
            return ResponseEntity.ok(new MessageResponse("Działka została pomyślnie usunięta"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<LandparcelDTO>> getLandparcels(
            @RequestParam(required = false) String searchString,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea) {

        return ResponseEntity.ok(landparcelFacade.getAvailableLandparcels(searchString, minArea, maxArea));
    }
}