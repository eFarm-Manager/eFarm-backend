package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest;
import com.efarm.efarmbackend.payload.response.BalanceResponse;
import com.efarm.efarmbackend.repository.finance.TransactionRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinanceFacade {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void addNewTransaction(NewTransactionRequest newTransactionRequest) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(transactionRepository.findNextFreeIdForFarm(loggedUserFarm.getId()), loggedUserFarm.getId());

        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, newTransactionRequest.getTransactionName());
        Transaction transaction = financeService.addNewTransactionData(transactionId, loggedUserFarm, newTransactionRequest);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void updateTransaction(Integer id, UpdateTransactionRequest updateTransactionRequest) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transakcja nie została znaleziona"));

        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, updateTransactionRequest.getTransactionName());
        financeService.updateTransactionProperties(transaction, updateTransactionRequest);
        transactionRepository.save(transaction);
    }

    public void deleteTransaction(Integer id) {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transakcja nie została znaleziona"));
        transactionRepository.delete(transaction);
    }

    public List<TransactionDTO> getTransactions(String searchQuery, LocalDate minDate, LocalDate maxDate,
                                                String financialCategoryString, String paymentStatusString,
                                                Double minAmount, Double maxAmount) {

        Farm loggedUserFarm = userService.getLoggedUserFarm();
        FinancialCategory financialCategory = financeService.getFinancialCategoryForFiltering(financialCategoryString);
        PaymentStatus paymentStatus = financeService.getPaymentStatusForFiltering(paymentStatusString);

        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                loggedUserFarm.getId(), searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);

        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public BalanceResponse getBalanceForLastYear() {
        Integer farmId = userService.getLoggedUserFarm().getId();
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        List<Transaction> transactions = financeService.getTransactionsByFarmAndDate(farmId, oneYearAgo, LocalDate.now());
        return financeService.calculateFarmBalance(transactions);
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId().getId());
        dto.setTransactionName(transaction.getTransactionName());
        dto.setAmount(transaction.getAmount());
        dto.setFinancialCategory(String.valueOf(transaction.getFinancialCategory().getName()));
        dto.setPaymentStatus(String.valueOf(transaction.getPaymentStatus().getName()));
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setPaymentDate(transaction.getPaymentDate());
        dto.setDescription(transaction.getDescription());
        return dto;
    }
}
