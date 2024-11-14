package com.efarm.efarmbackend.payload.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank(message = "Imię nie może być puste")
    @Size(min = 3, max = 30, message = "Imię musi mieć od 2 do 45 znaków")
    private String firstName;

    @NotBlank(message = "Nazwisko nie może być puste")
    @Size(min = 3, max = 40, message = "Nazwisko musi mieć od 2 do 45 znaków")
    private String lastName;

    @NotBlank(message = "Adres email nie może być pusty")
    @Email(message = "Niepoprawny format adresu email")
    @Size(max = 50, message = "Adres email może mieć maksymalnie 80 znaków")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\-]{9,11}$", message = "Niepoprawny format numeru telefonu")
    private String phoneNumber;

    @NotBlank(message = "Rola użytkownika nie może być pusta")
    @Pattern(regexp = "ROLE_FARM_OWNER|ROLE_FARM_MANAGER|ROLE_FARM_EQUIPMENT_OPERATOR", message = "Niepoprawna rola użytkownika")
    private String role;
}
