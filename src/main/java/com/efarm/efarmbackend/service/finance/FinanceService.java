package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
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
