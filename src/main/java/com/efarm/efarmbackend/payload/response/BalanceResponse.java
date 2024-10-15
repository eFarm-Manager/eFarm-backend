package com.efarm.efarmbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private Double totalIncome;
    private Double totalExpense;
    private Double balance;
    private Double toPay;
    private Double toReceive;
}
