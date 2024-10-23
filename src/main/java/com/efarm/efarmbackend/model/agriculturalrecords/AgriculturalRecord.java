package com.efarm.efarmbackend.model.agriculturalrecords;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Ewidencja")
public class AgriculturalRecord {

    @EmbeddedId
    private AgriculturalRecordId id;

    @MapsId("gospodarstwoIdgospodarstwo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Gospodarstwo_idGospodarstwo", nullable = false, referencedColumnName = "idGospodarstwo")
    private Farm farm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Sezony_idSezony", nullable = false)
    private Season season;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(column = @JoinColumn(name = "Dzialka_idDzialka", referencedColumnName = "idDzialka")),
            @JoinColumnOrFormula(formula = @JoinFormula(value = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo"))
    })
    private Landparcel landparcel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Uprawa_idUprawa", nullable = false)
    private Crop crop;

    @NotNull
    @Column(name = "powierzchnia", nullable = false)
    private Double area;

    @Size(max = 400)
    @Column(name = "opis", length = 400)
    private String description;

    public AgriculturalRecord(AgriculturalRecordId id, Season season, Landparcel landparcel, Crop crop, Double area, Farm farm, String description) {
        this.id = id;
        this.season = season;
        this.landparcel = landparcel;
        this.crop = crop;
        this.area = area;
        this.farm = farm;
        this.description = description;
    }
}