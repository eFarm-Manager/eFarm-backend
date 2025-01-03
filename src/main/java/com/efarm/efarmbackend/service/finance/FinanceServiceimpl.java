package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest;
import com.efarm.efarmbackend.payload.response.BalanceResponse;
import com.efarm.efarmbackend.repository.finance.FinancialCategoryRepository;
import com.efarm.efarmbackend.repository.finance.PaymentStatusRepository;
import com.efarm.efarmbackend.repository.finance.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FinanceServiceimpl implements FinanceService {

    private final FinancialCategoryRepository financialCategoryRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public Transaction addNewTransactionData(TransactionId transactionId,
                                             Farm loggedUserFarm,
                                             NewTransactionRequest request) {
        Transaction transaction = new Transaction(
                transactionId,
                loggedUserFarm,
                request
        );
        setNewTransactionPaymentStatus(
                transaction,
                request.getPaymentStatus()
        );
        setNewTransactionFinancialCategory(
                transaction,
                request.getFinancialCategory()
        );
        return transaction;
    }

    @Override
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

    @Override
    public void checkTransactionAlreadyExistsByName(Farm loggedUserFarm, String transactionName) throws Exception {
        if (transactionRepository.existsByTransactionNameAndFarmId(transactionName, loggedUserFarm.getId())) {
            throw new Exception("Transakcja o podanej nazwie już istnieje");
        }
    }

    @Override
    public FinancialCategory getFinancialCategoryForFiltering(String financialCategoryString) {
        FinancialCategory financialCategory;
        if (Objects.equals(financialCategoryString, "EXPENSE") ||
                Objects.equals(financialCategoryString, "INCOME")) {

            EFinancialCategory financialCategoryEnum = EFinancialCategory.valueOf(financialCategoryString.toUpperCase(Locale.ROOT));
            financialCategory = financialCategoryRepository.findByName(financialCategoryEnum);
        } else {
            financialCategory = null;
        }
        return financialCategory;
    }

    @Override
    public PaymentStatus getPaymentStatusForFiltering(String paymentStatusString) {
        PaymentStatus paymentStatus;
        if (Objects.equals(paymentStatusString, "PAID") ||
                Objects.equals(paymentStatusString, "UNPAID") ||
                Objects.equals(paymentStatusString, "AWAITING_PAYMENT")) {

            EPaymentStatus paymentStatusEnum = EPaymentStatus.valueOf(paymentStatusString.toUpperCase(Locale.ROOT));
            paymentStatus = paymentStatusRepository.findByName(paymentStatusEnum);
        } else {
            paymentStatus = null;
        }
        return paymentStatus;
    }

    @Override
    public List<Transaction> getTransactionsByFarmAndDate(Integer farmId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByFarmAndDate(farmId, startDate, endDate);
    }

    @Override
    @Transactional
    public void deleteAllTransactionsForFarm(Farm farm) {
        List<Transaction> transactions = transactionRepository.findByFarmId(farm.getId());
        transactionRepository.deleteAll(transactions);
    }

    @Override
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

            if (transaction.getPaymentStatus().getName() == EPaymentStatus.UNPAID &&
                    transaction.getFinancialCategory().getName() == EFinancialCategory.EXPENSE) {
                toPay += transaction.getAmount();
            }

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

    @Override
    public void setNewTransactionPaymentStatus(Transaction transaction, String paymentStatusName) {
        EPaymentStatus paymentStatusEnum;
        try {
            paymentStatusEnum = EPaymentStatus.valueOf(paymentStatusName);
        } catch (IllegalArgumentException e) {
            paymentStatusEnum = EPaymentStatus.UNPAID;
        }

        PaymentStatus paymentStatus = paymentStatusRepository.findByName(paymentStatusEnum);
        transaction.setPaymentStatus(paymentStatus);
    }

    @Override
    public void setNewTransactionFinancialCategory(Transaction transaction, String financialCategoryName) {
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
