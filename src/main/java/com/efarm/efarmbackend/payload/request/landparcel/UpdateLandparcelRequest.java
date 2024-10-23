package com.efarm.efarmbackend.payload.request.landparcel;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateLandparcelRequest {
    @Pattern(regexp = "STATUS_LEASE|STATUS_PRIVATELY_OWNED", message = "Status własności musi być jednym z: STATUS_LEASE, STATUS_PRIVATELY_OWNED")
    private String landOwnershipStatus;

    @Size(min = 3, max = 60, message = "Nazwa musi zawierać od 3 do 60 znaków")
    private String name;

    @DecimalMin(value = "-180.0", message = "Długość geograficzna musi być w przedziale od -180 do 180")
    @DecimalMax(value = "180.0", message = "Długość geograficzna musi być w przedziale od -180 do 180")
    private Double longitude;

    @DecimalMin(value = "-90.0", message = "Szerokość geograficzna musi być w przedziale od -90 do 90")
    @DecimalMax(value = "90.0", message = "Szerokość geograficzna musi być w przedziale od -90 do 90")
    private Double latitude;

    @DecimalMin(value = "0.01", message = "Powierzchnia działki musi być większa niż 0")
    @DecimalMax(value = "1000.0", message = "Powierzchnia działki nie może przekraczać 1000.0")
    private Double area;
}
