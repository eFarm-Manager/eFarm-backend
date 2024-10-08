package com.efarm.efarmbackend.model.user;

import com.efarm.efarmbackend.model.farm.Farm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Uzytkownik")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUzytkownik", nullable = false)
    private Integer id;

    @NotNull
    @Size(min = 2, max = 45)
    @Column(name = "imie")
    private String firstName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UprawnieniaUzytkownikow_idUprawnieniaUzytkownikow", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Gospodarstwo_idGospodarstwo", nullable = false, referencedColumnName = "idGospodarstwo")
    private Farm farm;

    @Size(max = 45)
    @NotNull
    @Column(name = "nazwisko", nullable = false, length = 45)
    private String lastName;

    @Size(max = 45)
    @NotNull
    @Column(name = "login", nullable = false, length = 45)
    private String username;

    @Size(max = 100)
    @NotNull
    @Column(name = "haslo", nullable = false, length = 100)
    private String password;

    @Size(max = 80)
    @NotNull
    @Column(name = "email", nullable = false, length = 80)
    private String email;

    @Column(name = "numerTelefonu")
    private String phoneNumber;

    @NotNull
    @Column(name = "czyAktywny", nullable = false)
    private Boolean isActive;

    public User(String firstName, String lastName, String username, String email, String password, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.isActive = true;
    }
}
