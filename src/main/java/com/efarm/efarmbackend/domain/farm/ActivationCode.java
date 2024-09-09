package com.efarm.efarmbackend.domain.farm;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "KodAktywacyjny")
public class ActivationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idKodAktywacyjny", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "kod", nullable = false, length = 45)
    private String code;

    @NotNull
    @Column(name = "dataWaznosci", nullable = false)
    private LocalDate expireDate;

    @NotNull
    @Column(name = "czyWykorzystany", nullable = false)
    private Boolean isUsed = false;

}