package com.efarm.efarmbackend.model.finance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "StatusPlatnosci")
public class PaymentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStatusPlatnosci", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "nazwaStatusu", nullable = false, length = 45)
    private String name;
}