package com.efarm.efarmbackend.model.landparcel;

import com.efarm.efarmbackend.model.farm.Farm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dzialka")
public class Landparcel {

    @EmbeddedId
    private LandparcelId id;

    @MapsId("gospodarstwoIdgospodarstwo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Gospodarstwo_idGospodarstwo", nullable = false, referencedColumnName = "idGospodarstwo")
    private Farm farm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "StanWlasnosciDzialki_idStanWlasnosciDzialki", nullable = false)
    private LandOwnershipStatus landOwnershipStatus;

    @Size(max = 45)
    @NotNull
    @Column(name = "wojewodztwo", nullable = false, length = 45)
    private String voivodeship;

    @Size(max = 60)
    @NotNull
    @Column(name = "powiat", nullable = false, length = 60)
    private String district;

    @Size(max = 60)
    @NotNull
    @Column(name = "gmina", nullable = false, length = 60)
    private String commune;

    @Size(max = 10)
    @NotNull
    @Column(name = "numerObrebuEwidencyjnego", nullable = false, length = 10)
    private String geodesyRegistrationDistrictNumber;

    @Size(max = 10)
    @NotNull
    @Column(name = "numerDzialki", nullable = false, length = 10)
    private String landparcelNumber;

    @NotNull
    @Column(name = "dlugoscGeograficzna", nullable = false)
    private Double longitude;

    @NotNull
    @Column(name = "szerokoscGeograficzna", nullable = false)
    private Double latitude;

    @NotNull
    @Column(name = "powierzchniaDzialki", nullable = false)
    private Double area;

    @NotNull
    @Column(name = "czyDostepna", nullable = false)
    private Boolean isAvailable = false;

}