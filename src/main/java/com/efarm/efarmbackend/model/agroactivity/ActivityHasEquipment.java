package com.efarm.efarmbackend.model.agroactivity;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Table(name = "ZabiegMaSprzet")
public class ActivityHasEquipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idZabiegMaSprzet", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(column = @JoinColumn(name = "Zabieg_idZabieg", referencedColumnName = "idZabieg")),
            @JoinColumnOrFormula(formula = @JoinFormula(value = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo"))
    })
    private AgroActivity agroActivity;

    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(column = @JoinColumn(name = "Sprzet_idSprzet", referencedColumnName = "idSprzet")),
            @JoinColumnOrFormula(formula = @JoinFormula(value = "Gospodarstwo_idGospodarstwo", referencedColumnName = "Gospodarstwo_idGospodarstwo"))
    })
    private FarmEquipment farmEquipment;
}