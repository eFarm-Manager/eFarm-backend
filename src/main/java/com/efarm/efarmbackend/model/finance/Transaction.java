package com.efarm.efarmbackend.model.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "ListaTransakcji")
public class Transaction {

    @EmbeddedId
    private TransactionId id;

    @MapsId("gospodarstwoIdgospodarstwo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Gospodarstwo_idGospodarstwo", nullable = false, referencedColumnName = "idGospodarstwo")
    private Farm farm;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "KategorieFinansowe_idKategorieFinansowe", nullable = false)
    private FinancialCategory financialCategory;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "StatusPlatnosci_idStatusPlatnosci", nullable = false)
    private PaymentStatus paymentStatus;

    @Size(max = 60)
    @NotNull
    @Column(name = "nazwaTransakcji", nullable = false, length = 60)
    private String transactionName;

    @NotNull
    @Column(name = "dataTransakcji", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "terminPlatnosci")
    private LocalDate paymentDate;

    @NotNull
    @Column(name = "kwotaTransakcji", nullable = false)
    private Double amount;

    @Size(max = 500)
    @Column(name = "opis", length = 500)
    private String description;

    public Transaction(TransactionId id, Farm farm, NewTransactionRequest request) {
        this.id = id;
        this.farm = farm;
        this.transactionName = request.getTransactionName();
        this.transactionDate = request.getTransactionDate();
        this.paymentDate = request.getPaymentDate();
        this.amount = request.getAmount();
        this.description = request.getDescription();
    }

    public TransactionId getId() {
        return id == null ? null : new TransactionId(id);
    }

    public void setId(TransactionId id) {
        this.id = id == null ? null : new TransactionId(id);
    }

    public void setFarm(Farm farm) {
        this.farm = farm == null ? null : new Farm(farm);
    }

    public FinancialCategory getFinancialCategory() {
        return financialCategory == null ? null : new FinancialCategory(financialCategory);
    }

    public void setFinancialCategory(FinancialCategory financialCategory) {
        this.financialCategory = financialCategory == null ? null : new FinancialCategory(financialCategory);
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus == null ? null : new PaymentStatus(paymentStatus);
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus == null ? null : new PaymentStatus(paymentStatus);
    }
}