package com.efarm.efarmbackend.model.equipment;

import com.efarm.efarmbackend.model.farm.Farm;
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
    private String brand;

    @Size(max = 45)
    @Column(name = "model", length = 45)
    private String model;

    @Column(name = "moc")
    private Integer power;

    @Column(name = "ladownosc")
    private Double capacity;

    @Column(name = "szerokoscRobocza")
    private Double workingWidth;

    @Column(name = "numerPolisy")
    private String insurancePolicyNumber;

    @Column(name = "dataWygasnieciaPolisy")
    private LocalDate insuranceExpirationDate;

    @Column(name = "dataWaznosciPrzegladu")
    private LocalDate inspectionExpireDate;

    public FarmEquipment(FarmEquipmentId farmEquipmentId, EquipmentCategory category, Farm farm) {
        this.isAvailable = true;
        this.category = category;
        this.farmIdFarm = farm;
        this.id = farmEquipmentId;
    }

    public FarmEquipmentId getId() {
        return id != null ? new FarmEquipmentId(id) : null;
    }

    public void setId(FarmEquipmentId id) {
        this.id = id != null ? new FarmEquipmentId(id) : null;
    }

    public void setFarmIdFarm(Farm farmIdFarm) {
        this.farmIdFarm = farmIdFarm != null ? new Farm(farmIdFarm) : null;
    }
}