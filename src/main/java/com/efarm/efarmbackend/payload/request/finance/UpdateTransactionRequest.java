package com.efarm.efarmbackend.payload.request.finance;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateTransactionRequest {

    @Size(min = 3, max = 60, message = "Nazwa transakcji musi zawierać od 3 do 60 znaków")
    private String transactionName;

    private LocalDate transactionDate;

    private LocalDate paymentDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Kwota musi być większa od zera")
    @Digits(integer = 10, fraction = 2, message = "Kwota musi być prawidłową liczbą")
    private Double amount;

    @Size(max = 500, message = "Opis musi mieć mniej niż 500 znaków")
    private String description;

    @Pattern(regexp = "EXPENSE|INCOME", message = "Nieprawidłowa kategoria finansowa")
    private String financialCategory;

    @Pattern(regexp = "AWAITING_PAYMENT|PAID|UNPAID", message = "Nieprawidłowy status płatności")
    private String paymentStatus;
}
