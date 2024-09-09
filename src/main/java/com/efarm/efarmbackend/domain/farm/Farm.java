package com.efarm.efarmbackend.domain.farm;

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

    public Farm(String farmName) {
        this.farmName = farmName;
    }
}