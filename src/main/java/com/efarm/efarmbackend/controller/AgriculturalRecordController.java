package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.payload.request.agriculturalrecord.CreateNewAgriculturalRecordRequest;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.UpdateAgriculturalRecordRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.agriculturalrecords.AgriculturalRecordFacade;
import com.efarm.efarmbackend.service.agriculturalrecords.AgriculturalRecordService;
import com.efarm.efarmbackend.service.agriculturalrecords.CropService;
import com.efarm.efarmbackend.service.agriculturalrecords.SeasonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/records")
public class AgriculturalRecordController {

    @Autowired
    private AgriculturalRecordFacade agriculturalRecordFacade;

    @Autowired
    private ValidationRequestService validationRequestService;

    @Autowired
    private SeasonService seasonService;

    @Autowired
    private CropService cropService;

    @Autowired
    private AgriculturalRecordService agriculturalRecordService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_EQUIPMENT_OPERATOR')")
    public ResponseEntity<?> getAgriculturalRecords(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String season
    ) {
        try {
            return ResponseEntity.ok(agriculturalRecordFacade.getAgriculturalRecords(season, searchQuery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/add-new-record")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> addAgriculturalRecord(@RequestBody @Valid CreateNewAgriculturalRecordRequest request, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            agriculturalRecordFacade.addAgriculturalRecord(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Pomyślnie dodano nową uprawę"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/generate-records-for-new-season")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> generateRecordsForSeason(@RequestParam String seasonName) {
        try {
            agriculturalRecordFacade.createRecordsForNewSeason(seasonName);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Ewidencje dla nowego sezonu zostały wygenerowane"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> updateAgriculturalRecord(@PathVariable Integer id, @RequestBody @Valid UpdateAgriculturalRecordRequest updateRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            agriculturalRecordService.updateAgriculturalRecord(id, updateRequest);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie zaktualizowano dane"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> deleteAgriculturalRecord(@PathVariable Integer id) {
        try {
            agriculturalRecordService.deleteAgriculturalRecord(id);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie usunięto wskazaną ewidencję"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/available-seasons")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<String>> getAvailableSeasons() {
        List<String> seasonNames = seasonService.getAvailableSeasons();
        return ResponseEntity.ok(seasonNames);
    }

    @GetMapping("/available-crops")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<String>> getAvailableCropNames() {
        List<String> cropNames = cropService.getAvailableCropNames();
        return ResponseEntity.ok(cropNames);
    }
}
