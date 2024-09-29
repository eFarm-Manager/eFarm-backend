package com.efarm.efarmbackend.model.equipment;

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
@Table(name = "Sprzet")
public class FarmEquipment {
    @EmbeddedId
    private FarmEquipmentId id;

    @MapsId("gospodarstwoIdgospodarstwo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Gospodarstwo_idGospodarstwo", nullable = false, referencedColumnName = "idGospodarstwo")
    private Farm farmIdFarm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "KategorieSprzetu_idKategorieSprzetu", nullable = false)
    private EquipmentCategory category;

    @Size(max = 60)
    @NotNull
    @Column(name = "nazwaSprzetu", nullable = false, length = 60)
    private String equipmentName;

    @NotNull
    @Column(name = "czyDostepny", nullable = false)
    private Boolean isAvailable = false;

    @Size(max = 45)
    @Column(name = "marka", length = 45)
    private String Brand;

    @Column(name = "moc")
    private Integer power;

    @Column(name = "ladownosc")
    private Double capacity;

    @Column(name = "szerokoscRobocza")
    private Double workingWidth;

    @Column(name = "numerPolisy", columnDefinition = "int UNSIGNED")
    private Long insurancePolicyNumber;

    @Column(name = "dataWygasnieciaPolisy")
    private LocalDate insuranceExpirationDate;

    @Column(name = "dataWaznosciPrzegladu")
    private LocalDate inspectionExpireDate;
}