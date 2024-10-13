package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest;
import com.efarm.efarmbackend.payload.response.BalanceResponse;
import com.efarm.efarmbackend.repository.finance.FinancialCategoryRepository;
import com.efarm.efarmbackend.repository.finance.PaymentStatusRepository;
import com.efarm.efarmbackend.repository.finance.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class FinanceService {

    @Autowired
    private FinancialCategoryRepository financialCategoryRepository;

    @Autowired
    private PaymentStatusRepository paymentStatusRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final Logger logger = LoggerFactory.getLogger(FinanceService.class);

    public Transaction addNewTransactionData(TransactionId transactionId, Farm loggedUserFarm, NewTransactionRequest newTransactionRequest) {
        Transaction transaction = new Transaction(
                transactionId,
                loggedUserFarm,
                newTransactionRequest.getTransactionName(),
                newTransactionRequest.getTransactionDate(),
                newTransactionRequest.getPaymentDate(),
                newTransactionRequest.getAmount(),
                newTransactionRequest.getDescription());

        setNewTransactionPaymentStatus(transaction, newTransactionRequest.getPaymentStatus());
        setNewTransactionFinancialCategory(transaction, newTransactionRequest.getFinancialCategory());
        return transaction;
    }

    public void updateTransactionProperties(Transaction transaction, UpdateTransactionRequest request) {
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
            EFinancialCategory financialCategoryEnum;
            try {
                financialCategoryEnum = EFinancialCategory.valueOf(request.getFinancialCategory().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                financialCategoryEnum = transaction.getFinancialCategory().getName();
            }
            if (transaction.getFinancialCategory().getName() != financialCategoryEnum) {
                FinancialCategory financialCategory = financialCategoryRepository.findByName(financialCategoryEnum);
                if (financialCategory == null) {
                    throw new RuntimeException("Kategoria finansowa nie została znaleziona: " + financialCategoryEnum);
                }
                transaction.setFinancialCategory(financialCategory);
            }
        }

        if (request.getPaymentStatus() != null) {
            EPaymentStatus paymentStatusEnum;
            try {
                paymentStatusEnum = EPaymentStatus.valueOf(request.getPaymentStatus().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                paymentStatusEnum = transaction.getPaymentStatus().getName();
            }
            if (transaction.getPaymentStatus().getName() != paymentStatusEnum) {
                PaymentStatus paymentStatus = paymentStatusRepository.findByName(paymentStatusEnum);
                if (paymentStatus == null) {
                    throw new RuntimeException("Status płatności nie został znaleziony: " + paymentStatusEnum);
                }
                transaction.setPaymentStatus(paymentStatus);
            }
        }
    }

    public void checkTransactionAlreadyExistsByName(Farm loggedUserFarm, String transactionName) throws Exception {
        if (transactionRepository.existsByTransactionNameAndFarmId(transactionName, loggedUserFarm.getId())) {
            throw new Exception("Transakcja o podanej nazwie już istnieje!");
        }
    }

    public FinancialCategory getFinancialCategoryForFiltering(String financialCategoryString) {
        FinancialCategory financialCategory;
        if (Objects.equals(financialCategoryString, "EXPENSE") ||
                Objects.equals(financialCategoryString, "INCOME")) {

            EFinancialCategory financialCategoryEnum = EFinancialCategory.valueOf(financialCategoryString.toUpperCase());
            financialCategory = financialCategoryRepository.findByName(financialCategoryEnum);
        } else {
            financialCategory = null;
        }
        return financialCategory;
    }

    public PaymentStatus getPaymentStatusForFiltering(String paymentStatusString) {
        PaymentStatus paymentStatus;
        if (Objects.equals(paymentStatusString, "PAID") ||
                Objects.equals(paymentStatusString, "UNPAID") ||
                Objects.equals(paymentStatusString, "AWAITING_PAYMENT")) {

            EPaymentStatus paymentStatusEnum = EPaymentStatus.valueOf(paymentStatusString.toUpperCase());
            paymentStatus = paymentStatusRepository.findByName(paymentStatusEnum);
        } else {
            paymentStatus = null;
        }
        return paymentStatus;
    }

    public List<Transaction> getTransactionsByFarmAndDate(Integer farmId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByFarmAndDate(farmId, startDate, endDate);
    }

    public BalanceResponse calculateFarmBalance(List<Transaction> transactions) {
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        double toPay = 0.0;
        double toReceive = 0.0;

        for (Transaction transaction : transactions) {
            if (transaction.getFinancialCategory().getName() == EFinancialCategory.INCOME) {
                totalIncome += transaction.getAmount();
            } else if (transaction.getFinancialCategory().getName() == EFinancialCategory.EXPENSE) {
                totalExpense += transaction.getAmount();
            }

            // Sum to pay (UNPAID, EXPENSE)
            if (transaction.getPaymentStatus().getName() == EPaymentStatus.UNPAID &&
                    transaction.getFinancialCategory().getName() == EFinancialCategory.EXPENSE) {
                toPay += transaction.getAmount();
            }

            // Sum to recive (UNPAID, INCOME)
            if (transaction.getPaymentStatus().getName() == EPaymentStatus.UNPAID &&
                    transaction.getFinancialCategory().getName() == EFinancialCategory.INCOME) {
                toReceive += transaction.getAmount();
            }
        }

        double balance = totalIncome - totalExpense;

        totalIncome = Math.round(totalIncome * 100) / 100.0;
        totalExpense = Math.round(totalExpense * 100) / 100.0;
        balance = Math.round(balance * 100.0) / 100.0;
        toPay = Math.round(toPay * 100.0) / 100.0;
        toReceive = Math.round(toReceive * 100.0) / 100.0;

        return new BalanceResponse(
                totalIncome, totalExpense, balance, toPay, toReceive
        );
    }

    private void setNewTransactionPaymentStatus(Transaction transaction, String paymentStatusName) {
        EPaymentStatus paymentStatusEnum;
        try {
            paymentStatusEnum = EPaymentStatus.valueOf(paymentStatusName);
        } catch (IllegalArgumentException e) {
            paymentStatusEnum = EPaymentStatus.UNPAID;
        }

        PaymentStatus paymentStatus = paymentStatusRepository.findByName(paymentStatusEnum);
        transaction.setPaymentStatus(paymentStatus);
    }

    private void setNewTransactionFinancialCategory(Transaction transaction, String financialCategoryName) {
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
