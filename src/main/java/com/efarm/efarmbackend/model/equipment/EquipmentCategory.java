package com.efarm.efarmbackend.model.equipment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Entity
@Table(name = "KategorieSprzetu")
public class EquipmentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idKategorieSprzetu", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "nazwaKategorii", nullable = false, length = 45)
    private String categoryName;
}