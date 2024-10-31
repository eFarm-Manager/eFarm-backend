package com.efarm.efarmbackend.model.agriculturalrecords;

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
@Table(name = "Sezony")
public class Season {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSezony", nullable = false)
    private Integer id;

    @Size(max = 15)
    @NotNull
    @Column(name = "nazwaSezonu", nullable = false, length = 15)
    private String name;
}