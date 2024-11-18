package com.efarm.efarmbackend.payload.request.landparcel;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddLandparcelRequest {
    @NotBlank(message = "Status własności nie może być pusty")
    @Pattern(regexp = "STATUS_LEASE|STATUS_PRIVATELY_OWNED", message = "Nieprawidłowy status własności")
    private String landOwnershipStatus;

    @NotBlank(message = "Nazwa nie może być pusta")
    @Size(min = 3, max = 60, message = "Nazwa musi zawierać od 3 do 60 znaków")
    private String name;

    @NotBlank(message = "Województwo nie może być puste")
    @Size(min = 3, max = 30, message = "Województwo musi zawierać od 3 do 30 znaków")
    private String voivodeship;

    @NotBlank(message = "Powiat nie może być pusty")
    @Size(min = 3, max = 30, message = "Powiat musi zawierać od 3 do 30 znaków")
    private String district;

    @NotBlank(message = "Gmina nie może być pusta")
    @Size(min = 3, max = 30, message = "Gmina musi zawierać od 3 do 30 znaków")
    private String commune;

    @NotBlank(message = "Numer obrębu ewidencyjnego nie może być pusty")
    @Size(min = 1, max = 10, message = "Numer obrębu ewidencyjnego musi zawierać od 1 do 10 znaków")
    private String geodesyDistrictNumber;

    @NotBlank(message = "Numer działki nie może być pusty")
    @Size(min = 1, max = 20, message = "Numer działki musi zawierać od 1 do 20 znaków")
    private String landparcelNumber;

    @NotBlank(message = "Numer działki nie może być pusty")
    @Size(min = 1, max = 45, message = "Numer ewidencyjny działki musi być prawidłowy")
    private String geodesyLandparcelNumber;

    @NotNull(message = "Długość geograficzna nie może być pusta")
    @DecimalMin(value = "-180.0", message = "Długość geograficzna musi być w przedziale od -180 do 180")
    @DecimalMax(value = "180.0", message = "Długość geograficzna musi być w przedziale od -180 do 180")
    private Double longitude;

    @NotNull(message = "Szerokość geograficzna nie może być pusta.")
    @DecimalMin(value = "-90.0", message = "Szerokość geograficzna musi być w przedziale od -90 do 90")
    @DecimalMax(value = "90.0", message = "Szerokość geograficzna musi być w przedziale od -90 do 90")
    private Double latitude;

    @NotNull(message = "Powierzchnia nie może być pusta.")
    @DecimalMin(value = "0.0001", message = "Nie możesz podać wartości mniejszej niż 0.0001 ha")
    @Digits(integer = 7, fraction = 4, message = "Możesz podać maksymalnie 4 cyfry po przecinku")
    private Double area;
}
