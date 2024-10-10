package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateFinanceRequest;
import com.efarm.efarmbackend.repository.finance.FinancialCategoryRepository;
import com.efarm.efarmbackend.repository.finance.PaymentStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinanceService {

    @Autowired
    private FinancialCategoryRepository financialCategoryRepository;

    @Autowired
    private PaymentStatusRepository paymentStatusRepository;


    public Transaction addNewTransactionData(TransactionId transactionId, Farm loggedUserFarm, NewTransactionRequest newTransactionRequest) {
        Transaction transaction = new Transaction(
                transactionId,
                loggedUserFarm,
                newTransactionRequest.getTransactionName(),
                newTransactionRequest.getTransactionDate(),
                newTransactionRequest.getPaymentDate(),
                newTransactionRequest.getAmount(),
                newTransactionRequest.getDescription());

        setTransactionPaymentStatus(transaction, newTransactionRequest.getPaymentStatus());
        setTransactionFinancialCategory(transaction, newTransactionRequest.getFinancialCategory());
        return transaction;
    }

    public void updateTransactionProperties(Transaction transaction, UpdateFinanceRequest request) {
        if (request.getTransactionName() != null) {
            transaction.setTransactionName(request.getTransactionName());
        }
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }
        if (request.getPaymentDate() != null) {
            transaction.setPaymentDate(request.getPaymentDate());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getFinancialCategory() != null) {
            transaction.setFinancialCategory(request.getFinancialCategory());
        }
        if (request.getPaymentStatus() != null) {
            transaction.setPaymentStatus(request.getPaymentStatus());
        }
    }

    private void setTransactionPaymentStatus(Transaction transaction, String paymentStatusName) {
        EPaymentStatus paymentStatusEnum;
        try {
            paymentStatusEnum = EPaymentStatus.valueOf(paymentStatusName);
        } catch (IllegalArgumentException e) {
            paymentStatusEnum = EPaymentStatus.UNPAID;
        }

        PaymentStatus paymentStatus = paymentStatusRepository.findByName(paymentStatusEnum);
        transaction.setPaymentStatus(paymentStatus);
    }

    private void setTransactionFinancialCategory(Transaction transaction, String financialCategoryName) {
        EFinancialCategory financialCategoryEnum;
        try {
            financialCategoryEnum = EFinancialCategory.valueOf(financialCategoryName);
        } catch (IllegalArgumentException e) {
            financialCategoryEnum = EFinancialCategory.EXPENSE;
        }

        FinancialCategory financialCategory = financialCategoryRepository.findByName(financialCategoryEnum);
        transaction.setFinancialCategory(financialCategory);
    }
}
