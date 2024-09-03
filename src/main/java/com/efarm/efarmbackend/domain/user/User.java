package com.efarm.efarmbackend.domain.user;

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

//    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UprawnieniaUzytkownikow_idUprawnieniaUzytkownikow", nullable = false)
    private Role role;

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
    private Integer phoneNumber;

    public User(String username, String email, String password) {
        this.firstName = "No name";
        this.lastName = "No last name";
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
