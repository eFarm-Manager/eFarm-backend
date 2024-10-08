package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.landparcel.LandparcelFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/landparcel")
public class LandparcelController {

    @Autowired
    private LandparcelFacade landparcelFacade;

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> addNewLandparcel(@RequestBody LandparcelDTO landparcelDTO) {
        try {
            landparcelFacade.addNewLandparcel(landparcelDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Pomyślnie dodano nową działkę"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Błąd dodawania działki: " + e.getMessage()));
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
    public ResponseEntity<?> updateLandparcel(@PathVariable Integer landparcelId, @RequestBody LandparcelDTO landparcelDTO) {
        try {
            landparcelFacade.updateLandparcel(landparcelId, landparcelDTO);
            return ResponseEntity.ok(new MessageResponse("Dane działki zostały pomyślmnie zaktualizowane"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{landparcelId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> deleteLandparcel(@PathVariable Integer landparcelId) {
        try {
            landparcelFacade.deleteLandparcel(landparcelId);
            return ResponseEntity.ok(new MessageResponse("Działka została pomyślnie usunięta"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}