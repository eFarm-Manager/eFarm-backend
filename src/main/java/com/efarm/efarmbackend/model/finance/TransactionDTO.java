package com.efarm.efarmbackend.model.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TransactionDTO {
    private Integer id;
    private String transactionName;
    private Double amount;
    private String financialCategory;
    private String paymentStatus;
    private LocalDate transactionDate;
    private LocalDate paymentDate;
    private String description;

    public TransactionDTO(Transaction transaction) {
        this.id = transaction.getId().getId();
        this.transactionName = transaction.getTransactionName();
        this.amount = transaction.getAmount();
        this.financialCategory = String.valueOf(transaction.getFinancialCategory().getName());
        this.paymentStatus = String.valueOf(transaction.getPaymentStatus().getName());
        this.transactionDate = transaction.getTransactionDate();
        this.paymentDate = transaction.getPaymentDate();
        this.description = transaction.getDescription();
    }
}

