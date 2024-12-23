package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest;
import com.efarm.efarmbackend.payload.response.BalanceResponse;
import com.efarm.efarmbackend.repository.finance.TransactionRepository;
import com.efarm.efarmbackend.service.user.UserAuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FinanceFacade {

    private final FinanceService financeService;
    private final TransactionRepository transactionRepository;
    private final UserAuthenticationService userAuthenticationService;

    @Transactional
    public void addNewTransaction(NewTransactionRequest newTransactionRequest) throws Exception {
        Farm loggedUserFarm = userAuthenticationService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(transactionRepository.findNextFreeIdForFarm(loggedUserFarm.getId()), loggedUserFarm.getId());

        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, newTransactionRequest.getTransactionName());
        Transaction transaction = financeService.addNewTransactionData(transactionId, loggedUserFarm, newTransactionRequest);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void updateTransaction(Integer id, UpdateTransactionRequest updateTransactionRequest) throws Exception {
        Farm loggedUserFarm = userAuthenticationService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transakcja nie została znaleziona"));

        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, updateTransactionRequest.getTransactionName());
        financeService.updateTransactionProperties(transaction, updateTransactionRequest);
        transactionRepository.save(transaction);
    }

    public void deleteTransaction(Integer id) {
        Farm loggedUserFarm = userAuthenticationService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transakcja nie została znaleziona"));
        transactionRepository.delete(transaction);
    }

    public List<TransactionDTO> getTransactions(String searchQuery, LocalDate minDate, LocalDate maxDate,
                                                String financialCategoryString, String paymentStatusString,
                                                Double minAmount, Double maxAmount) {

        Farm loggedUserFarm = userAuthenticationService.getLoggedUserFarm();
        FinancialCategory financialCategory = financeService.getFinancialCategoryForFiltering(financialCategoryString);
        PaymentStatus paymentStatus = financeService.getPaymentStatusForFiltering(paymentStatusString);
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                loggedUserFarm.getId(), searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);

        return transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
    }

    public BalanceResponse getBalanceForLastYear() {
        Integer farmId = userAuthenticationService.getLoggedUserFarm().getId();
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        List<Transaction> transactions = financeService.getTransactionsByFarmAndDate(farmId, oneYearAgo, LocalDate.now());
        return financeService.calculateFarmBalance(transactions);
    }
}
