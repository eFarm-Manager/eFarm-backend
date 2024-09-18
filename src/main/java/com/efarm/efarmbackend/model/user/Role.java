package com.efarm.efarmbackend.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "UprawnieniaUzytkownikow")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUprawnieniaUzytkownikow", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "nazwaUprawnien", nullable = false, length = 45)
    private ERole name;

    @Override
    public String toString() {
        return switch (name) {
            case ROLE_FARM_OWNER -> "Owner";
            case ROLE_FARM_MANAGER -> "Manager";
            default -> "Operator";
        };
    }
}