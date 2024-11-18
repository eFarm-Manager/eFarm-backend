package com.efarm.efarmbackend.payload.request.landparcel;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLandparcelRequest {
    @Pattern(regexp = "STATUS_LEASE|STATUS_PRIVATELY_OWNED", message = "Nieprawidłowy status własności")
    private String landOwnershipStatus;

    @Size(min = 3, max = 60, message = "Nazwa musi zawierać od 3 do 60 znaków")
    private String name;

    @DecimalMin(value = "-180.0", message = "Długość geograficzna musi być w przedziale od -180 do 180")
    @DecimalMax(value = "180.0", message = "Długość geograficzna musi być w przedziale od -180 do 180")
    private Double longitude;

    @DecimalMin(value = "-90.0", message = "Szerokość geograficzna musi być w przedziale od -90 do 90")
    @DecimalMax(value = "90.0", message = "Szerokość geograficzna musi być w przedziale od -90 do 90")
    private Double latitude;

    @DecimalMin(value = "0.0001", message = "Nie możesz podać wartości mniejszej niż 0.0001 ha")
    @Digits(integer = 7, fraction = 4, message = "Możesz podać maksymalnie 4 cyfry po przecinku")
    private Double area;
}
