package com.efarm.efarmbackend.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
}