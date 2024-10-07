package com.efarm.efarmbackend.model.farmfield;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "uprawa")
public class Crop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUprawa", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "nazwaUprawy", nullable = false, length = 45)
    private String name;

    public Crop(Crop crop) {
        this.id = crop.id;
        this.name = crop.name;
    }
}