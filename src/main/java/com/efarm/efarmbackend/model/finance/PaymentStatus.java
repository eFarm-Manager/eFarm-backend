package com.efarm.efarmbackend.model.finance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "StatusPlatnosci")
public class PaymentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStatusPlatnosci", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "nazwaStatusu", nullable = false, length = 45)
    private EPaymentStatus name;

    public PaymentStatus(PaymentStatus paymentStatus) {
        this.id = paymentStatus.id;
        this.name = paymentStatus.name;
    }
}