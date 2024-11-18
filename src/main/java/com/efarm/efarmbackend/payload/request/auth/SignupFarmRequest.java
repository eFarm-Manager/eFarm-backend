package com.efarm.efarmbackend.payload.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupFarmRequest {
    @NotBlank(message = "Imię nie może być puste")
    @Size(min = 3, max = 30, message = "Imię musi mieć od 3 do 30 znaków")
    private String firstName;

    @NotBlank(message = "Nazwisko nie może być puste")
    @Size(min = 3, max = 40, message = "Nazwisko musi mieć od 3 do 40 znaków")
    private String lastName;

    @NotBlank(message = "Nazwa użytkownika nie może być pusta")
    @Size(min = 6, max = 30, message = "Nazwa użytkownika musi mieć od 6 do 30 znaków")
    private String username;

    @NotBlank(message = "Adres email nie może być pusty")
    @Email(message = "Niepoprawny format adresu email")
    @Size(max = 50, message = "Adres email może mieć maksymalnie 50 znaków")
    private String email;

    @NotBlank(message = "Hasło nie może być puste")
    @Size(min = 6, max = 40, message = "Hasło musi mieć od 6 do 40 znaków")
    private String password;

    @Pattern(regexp = "^$|^\\+?[0-9\\-]{9,12}$", message = "Niepoprawny format numeru telefonu")
    @Size(max = 12, message = "Numer telefonu może mieć maksymalnie 12 znaków")
    private String phoneNumber;

    @NotBlank(message = "Nazwa farmy nie może być pusta")
    @Size(min = 6, max = 45, message = "Nazwa farmy musi mieć od 6 do 45 znaków")
    private String farmName;

    @NotBlank(message = "Kod aktywacyjny nie może być pusty")
    @Size(min = 6, max = 20, message = "Kod aktywacyjny musi mieć od 6 do 20 znaków")
    private String activationCode;
}