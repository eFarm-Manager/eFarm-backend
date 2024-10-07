package com.efarm.efarmbackend.model.farmfield;

import com.efarm.efarmbackend.model.farm.Farm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pole")
public class Farmfield {

    @EmbeddedId
    private FarmfieldId id;

    @MapsId("gospodarstwoIdgospodarstwo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Gospodarstwo_idGospodarstwo", nullable = false, referencedColumnName = "idGospodarstwo")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Uprawa_idUprawa")
    private Crop crop;

    @Size(max = 45)
    @NotNull
    @Column(name = "nazwaPola", nullable = false, length = 45)
    private String name;

    @NotNull
    @Column(name = "powierzchnia", nullable = false)
    private Double area;

    @NotNull
    @Column(name = "czyDostepny", nullable = false)
    private Boolean isAvailable = false;

    @Size(max = 500)
    @Column(name = "opis", length = 500)
    private String description;

}