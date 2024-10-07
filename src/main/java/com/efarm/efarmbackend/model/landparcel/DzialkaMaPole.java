package com.efarm.efarmbackend.model.landparcel;

import com.efarm.efarmbackend.model.farmfield.Farmfield;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class DzialkaMaPole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDzialkaMaPole", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "Dzialka_idDzialka", referencedColumnName = "idDzialka", nullable = false),
            @JoinColumn(name = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo", nullable = false)
    })
    private Landparcel dzialka;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "Pole_idPole", referencedColumnName = "idPole", nullable = false),
            @JoinColumn(name = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo", nullable = false)
    })
    private Farmfield pole;

}