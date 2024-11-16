package com.efarm.efarmbackend.model.agroactivity;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.request.agroactivity.NewAgroActivityRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Zabieg")
public class AgroActivity {
    @EmbeddedId
    private AgroActivityId id;

    @MapsId("gospodarstwoIdgospodarstwo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Gospodarstwo_idGospodarstwo", nullable = false, referencedColumnName = "idGospodarstwo")
    private Farm farm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "KategorieZabiegow_idKategorieZabiegow", nullable = false)
    private ActivityCategory activityCategory;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(column = @JoinColumn(name = "Ewidencja_idEwidencja", referencedColumnName = "idEwidencja")),
            @JoinColumnOrFormula(formula = @JoinFormula(value = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo"))
    })
    private AgriculturalRecord agriculturalRecord;

    @Size(max = 45)
    @NotNull
    @Column(name = "nazwaZabiegu", nullable = false, length = 45)
    private String name;

    @NotNull
    @Column(name = "dataWykonania", nullable = false)
    private Instant date;

    @NotNull
    @Column(name = "czyZrealizowane", nullable = false)
    private Boolean isCompleted = false;

    @Size(max = 100)
    @Column(name = "uzyteSrodki", length = 100)
    private String usedSubstances;

    @Size(max = 45)
    @Column(name = "zastosowanaDawka", length = 45)
    private String appliedDose;

    @Size(max = 150)
    @Column(name = "opis", length = 150)
    private String description;

    public AgroActivity(AgroActivityId id, ActivityCategory activityCategory, AgriculturalRecord agriculturalRecord, NewAgroActivityRequest request) {
        this.id = id;
        this.activityCategory = activityCategory;
        this.agriculturalRecord = agriculturalRecord;
        this.name = request.getName();
        this.usedSubstances = request.getUsedSubstances();
        this.appliedDose = request.getAppliedDose();
        this.description = request.getDescription();
    }
}