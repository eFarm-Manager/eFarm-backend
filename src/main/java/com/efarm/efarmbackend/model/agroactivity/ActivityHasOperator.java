package com.efarm.efarmbackend.model.agroactivity;

import com.efarm.efarmbackend.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

@Getter
@Setter
@Entity
@Table(name = "ZabiegMaUzytkownika")
public class ActivityHasOperator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idZabiegMaUzytkownika", nullable = false)
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
    @JoinColumn(name = "Uzytkownik_idUzytkownik", nullable = false, referencedColumnName = "idUzytkownik")
    private User user;
}