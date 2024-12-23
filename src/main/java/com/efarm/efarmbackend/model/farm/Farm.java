package com.efarm.efarmbackend.model.farm;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Gospodarstwo")
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idGospodarstwo", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "Adres_idAdres", nullable = false)
    private Integer idAddress;

    @NotNull
    @Column(name = "KodAktywacyjny_IdKodAktywacyjny", nullable = false)
    private Integer idActivationCode;

    @Size(max = 45)
    @NotNull
    @Column(name = "nazwaGospodarstwa", nullable = false, length = 45)
    private String farmName;

    @Size(max = 30)
    @Column(name = "nrGospodarstwa", length = 30)
    private String farmNumber;

    @Size(max = 30)
    @Column(name = "nrPaszowy", length = 30)
    private String feedNumber;

    @Size(max = 30)
    @Column(name = "nrRejestruSanitarnego", length = 30)
    private String sanitaryRegisterNumber;

    @NotNull
    @Column(name = "czyAktywne", nullable = false)
    private Boolean isActive;

    public Farm(String farmName, Integer idAddress, Integer idActivationCode, Boolean isActive) {
        this.farmName = farmName;
        this.idAddress = idAddress;
        this.idActivationCode = idActivationCode;
        this.isActive = isActive;
    }

    public Farm(Farm other) {
        this.id = other.id;
        this.idAddress = other.idAddress;
        this.idActivationCode = other.idActivationCode;
        this.farmName = other.farmName;
        this.farmNumber = other.farmNumber;
        this.feedNumber = other.feedNumber;
        this.sanitaryRegisterNumber = other.sanitaryRegisterNumber;
        this.isActive = other.isActive;
    }
}