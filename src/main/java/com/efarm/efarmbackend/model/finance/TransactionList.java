package com.efarm.efarmbackend.model.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "ListaTransakcji")
public class TransactionList {

    @EmbeddedId
    private TransactionListId id;

    @MapsId("gospodarstwoIdgospodarstwo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Gospodarstwo_idGospodarstwo", nullable = false, referencedColumnName = "idGospodarstwo")
    private Farm farm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "KategorieFinansowe_idKategorieFinansowe", nullable = false)
    private FinancialCategory financialCategory;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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
    private LocalDate terminPlatnosci;

    @NotNull
    @Column(name = "kwotaTransakcji", nullable = false)
    private Double amount;

    @Size(max = 500)
    @Column(name = "opis", length = 500)
    private String description;
}