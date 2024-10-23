package com.efarm.efarmbackend.model.agriculturalrecords;

import com.efarm.efarmbackend.model.landparcel.Landparcel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Ewidencja")
public class AgriculturalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEwidencja", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Sezony_idSezony", nullable = false)
    private Season season;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "Dzialka_idDzialka", referencedColumnName = "idDzialka", nullable = false),
            @JoinColumn(name = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo", nullable = false)
    })
    private Landparcel landparcel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Uprawa_idUprawa", nullable = false)
    private Crop crop;

    @NotNull
    @Column(name = "powierzchnia", nullable = false)
    private Double area;

    public AgriculturalRecord(Season season, Landparcel landparcel, Crop crop, Double area) {
        this.season = season;
        this.landparcel = landparcel;
        this.crop = crop;
        this.area = area;
    }
}