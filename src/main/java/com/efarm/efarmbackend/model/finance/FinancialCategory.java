package com.efarm.efarmbackend.model.finance;

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
@Table(name = "KategorieFinansowe")
public class FinancialCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idKategorieFinansowe", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "nazwaKategorii", nullable = false, length = 45)
    private EFinancialCategory name;

    public FinancialCategory(FinancialCategory financialCategory) {
        this.id = financialCategory.id;
        this.name = financialCategory.name;
    }
}