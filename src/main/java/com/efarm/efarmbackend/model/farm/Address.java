package com.efarm.efarmbackend.model.farm;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Adres")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAdres", nullable = false)
    private Integer id;

    @Size(max = 45)
    @Column(name = "ulica", length = 45)
    private String street;

    @Size(max = 12)
    @Column(name = "numerDomu", length = 12)
    private String buildingNumber;

    @Size(max = 12)
    @Column(name = "kodPocztowy", length = 12)
    private String zipCode;

    @Size(max = 45)
    @Column(name = "miejscowosc", length = 45)
    private String city;
}