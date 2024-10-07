package com.efarm.efarmbackend.model.landparcel;

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
@Table(name = "stanWlasnosciDzialki")
public class LandOwnershipStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStanWlasnosciDzialki", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "nazwaStanuWlasnosci", nullable = false, length = 45)
    private ELandOwnershipStatus ownershipStatus;


    public LandOwnershipStatus(LandOwnershipStatus ownershipStatus) {
        this.id = ownershipStatus.id;
        this.ownershipStatus = ownershipStatus.ownershipStatus;
    }

    @Override
    public String toString() {
        return switch (ownershipStatus) {
            case STATUS_PRIVATELY_OWNED -> "Własna";
            case STATUS_LEASE -> "Wydzierżawiona";
        };
    }
}