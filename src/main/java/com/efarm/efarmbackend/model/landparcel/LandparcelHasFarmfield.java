package com.efarm.efarmbackend.model.landparcel;

import com.efarm.efarmbackend.model.farmfield.Farmfield;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dzialkaMaPole")
public class LandparcelHasFarmfield {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDzialkaMaPole", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "Dzialka_idDzialka", referencedColumnName = "idDzialka"),
            @JoinColumn(name = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo")
    })
    private Landparcel landparcel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "Pole_idPole", referencedColumnName = "idPole"),
            @JoinColumn(name = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo")
    })
    private Farmfield farmField;


    public Landparcel getLandparcel() {
        return landparcel == null ? null : new Landparcel(landparcel);
    }

    public void setLandparcel(Landparcel landparcel) {
        this.landparcel = landparcel == null ? null : new Landparcel(landparcel);
    }

    public Farmfield getFarmField() {
        return farmField == null ? null : new Farmfield(farmField);
    }

    public void setFarmField(Farmfield farmField) {
        this.farmField = farmField == null ? null : new Farmfield(farmField);
    }
}