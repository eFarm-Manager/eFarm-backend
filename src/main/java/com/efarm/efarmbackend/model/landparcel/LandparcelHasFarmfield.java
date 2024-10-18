package com.efarm.efarmbackend.model.landparcel;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import com.efarm.efarmbackend.model.farmfield.Farmfield;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "DzialkaMaPole")
public class LandparcelHasFarmfield {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDzialkaMaPole", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(column = @JoinColumn(name = "Dzialka_idDzialka", referencedColumnName = "idDzialka")),
        @JoinColumnOrFormula(formula = @JoinFormula(value = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo"))
    })
    private Landparcel landparcel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(column = @JoinColumn(name = "Pole_idPole", referencedColumnName = "idPole")),
        @JoinColumnOrFormula(formula = @JoinFormula(value = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo"))
    })
    private Farmfield farmField;

    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;

    public Landparcel getLandparcel() {
        return landparcel == null ? null : new Landparcel(landparcel);
    }

    public void setLandparcel(Landparcel landparcel) {
        this.landparcel = landparcel == null ? null : new Landparcel(landparcel);
        if (landparcel != null) {
            this.farmId = landparcel.getFarm().getId(); // not sure it works
        }
    }

    public Farmfield getFarmField() {
        return farmField == null ? null : new Farmfield(farmField);
    }

    public void setFarmField(Farmfield farmField) {
        this.farmField = farmField == null ? null : new Farmfield(farmField);
        if (farmField != null) {
            this.farmId = farmField.getFarm().getId(); // not sure it works
        }
    }
}