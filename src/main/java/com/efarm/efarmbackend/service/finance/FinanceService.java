package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.FinancialCategory;
import com.efarm.efarmbackend.model.finance.PaymentStatus;
import com.efarm.efarmbackend.model.finance.Transaction;
import com.efarm.efarmbackend.model.finance.TransactionId;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest;
import com.efarm.efarmbackend.payload.response.BalanceResponse;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface FinanceService {
    Transaction addNewTransactionData(TransactionId transactionId, Farm loggedUserFarm, NewTransactionRequest newTransactionRequest);

    void updateTransactionProperties(Transaction transaction, UpdateTransactionRequest request);

    void checkTransactionAlreadyExistsByName(Farm loggedUserFarm, String transactionName) throws Exception;

    FinancialCategory getFinancialCategoryForFiltering(String financialCategoryString);

    PaymentStatus getPaymentStatusForFiltering(String paymentStatusString);

    List<Transaction> getTransactionsByFarmAndDate(Integer farmId, LocalDate startDate, LocalDate endDate);

    @Transactional
    void deleteAllTransactionsForFarm(Farm farm);

    BalanceResponse calculateFarmBalance(List<Transaction> transactions);

    void setNewTransactionPaymentStatus(Transaction transaction, String paymentStatusName);

    void setNewTransactionFinancialCategory(Transaction transaction, String financialCategoryName);
}
