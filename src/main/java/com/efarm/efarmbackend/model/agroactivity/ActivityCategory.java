package com.efarm.efarmbackend.model.agroactivity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "KategorieZabiegow")
public class ActivityCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idKategorieZabiegow", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "nazwaKategorii", nullable = false, length = 45)
    private String name;
}