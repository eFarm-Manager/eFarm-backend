package com.efarm.efarmbackend.model.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String transactionName;
    private Double amount;
    private String financialCategory;
    private String paymentStatus;
    private LocalDate transactionDate;
    private LocalDate paymentDate;
    private String description;
}

