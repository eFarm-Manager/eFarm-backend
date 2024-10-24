package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.payload.request.agriculturalrecord.CreateNewAgriculturalRecordRequest;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.UpdateAgriculturalRecordRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.agriculturalrecords.AgriculturalRecordFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
public class AgriculturalRecordController {

    @Autowired
    private AgriculturalRecordFacade agriculturalRecordFacade;

    @Autowired
    private ValidationRequestService validationRequestService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_EQUIPMENT_OPERATOR')")
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

    @PostMapping("/add-new-record")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> addAgriculturalRecord(@RequestBody @Valid CreateNewAgriculturalRecordRequest request, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequestWithException(bindingResult);
            agriculturalRecordFacade.addAgriculturalRecord(request);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie dodano nową uprawę"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAgriculturalRecord(@PathVariable Integer id, @RequestBody @Valid UpdateAgriculturalRecordRequest updateRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequestWithException(bindingResult);
            agriculturalRecordFacade.updateAgriculturalRecord(id, updateRequest);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie zaktualizowano dane"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
