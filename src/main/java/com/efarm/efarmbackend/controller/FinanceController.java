package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.finance.FinanceFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    @Autowired
    private FinanceFacade financeFacade;

    @Autowired
    private ValidationRequestService validationRequestService;

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> addNewTransaction(@Valid @RequestBody NewTransactionRequest newTransactionRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequestWithException(bindingResult);
            financeFacade.addNewTransaction(newTransactionRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Pomyślnie dodano nową transakcję"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}