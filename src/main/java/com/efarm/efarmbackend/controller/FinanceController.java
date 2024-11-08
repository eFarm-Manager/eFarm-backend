package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.finance.TransactionDTO;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest;
import com.efarm.efarmbackend.payload.response.BalanceResponse;
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

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private FinanceFacade financeFacade;

    @Autowired
    private ValidationRequestService validationRequestService;

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> addNewTransaction(@Valid @RequestBody NewTransactionRequest newTransactionRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            financeFacade.addNewTransaction(newTransactionRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Pomyślnie dodano nową transakcję"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> updateTransaction(@PathVariable Integer id, @Valid @RequestBody UpdateTransactionRequest updateRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            financeFacade.updateTransaction(id, updateRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new MessageResponse("Pomyślnie zaktualizowano transakcję"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> deleteTransaction(@PathVariable Integer id) {
        try {
            financeFacade.deleteTransaction(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new MessageResponse("Transakcja została usunięta"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<TransactionDTO>> getTransactions(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) LocalDate minDate,
            @RequestParam(required = false) LocalDate maxDate,
            @RequestParam(required = false) String financialCategory,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        List<TransactionDTO> transactions = financeFacade.getTransactions(searchQuery, minDate, maxDate,
                financialCategory, paymentStatus, minAmount, maxAmount);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/balance")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<BalanceResponse> getBalance() {
            BalanceResponse balanceResponse = financeFacade.getBalanceForLastYear();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(balanceResponse);
    }
}
