package com.efarm.efarmbackend.payload.request.landparcel;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddLandparcelRequest {
    @NotBlank(message = "Status własności nie może być pusty")
    @Pattern(regexp = "STATUS_LEASE|STATUS_PRIVATELY_OWNED", message = "Status własności musi być jednym z: STATUS_LEASE, STATUS_PRIVATELY_OWNED")
    private String landOwnershipStatus;

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
    private String geodesyRegistrationDistrictNumber;

    @NotBlank(message = "Numer działki nie może być pusty")
    @Size(min = 1, max = 20, message = "Numer działki musi zawierać od 1 do 20 znaków")
    private String landparcelNumber;

    @NotNull(message = "Długość geograficzna nie może być pusta")
    @DecimalMin(value = "-180.0", message = "Długość geograficzna musi być w przedziale od -180 do 180")
    @DecimalMax(value = "180.0", message = "Długość geograficzna musi być w przedziale od -180 do 180")
    private Double longitude;

    @NotNull(message = "Szerokość geograficzna nie może być pusta.")
    @DecimalMin(value = "-90.0", message = "Szerokość geograficzna musi być w przedziale od -90 do 90")
    @DecimalMax(value = "90.0", message = "Szerokość geograficzna musi być w przedziale od -90 do 90")
    private Double latitude;

    @NotNull(message = "Powierzchnia nie może być pusta.")
    @DecimalMin(value = "0.01", message = "Powierzchnia działki musi być większa niż 0")
    @DecimalMax(value = "1000.0", message = "Powierzchnia działki nie może przekraczać 100000")
    private Double area;
}
