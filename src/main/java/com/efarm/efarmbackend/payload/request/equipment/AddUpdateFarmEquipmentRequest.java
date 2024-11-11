package com.efarm.efarmbackend.payload.request.equipment;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddUpdateFarmEquipmentRequest {
    private Integer equipmentId;

    @Size(min = 6, max = 60, message = "Nazwa sprzętu musi zawierać od 6 do 60 znaków")
    private String equipmentName;

    @Size(min = 6, max = 45, message = "Kategoria musi być prawidłowa")
    private String category;

    @Size(min = 2, max = 45, message = "Marka musi zawierać od 2 do 45 znaków")
    private String brand;

    @Size(min = 2, max = 45, message = "Model musi zawierać od 2 do 45 znaków")
    private String model;

    @Min(value = 1, message = "Moc musi być liczbą dodatnią")
    @Max(value = 1400, message = "Moc nie może przekraczać 1400")
    private Integer power;

    @DecimalMin(value = "0.1", message = "Pojemność musi być większa niż 0")
    @DecimalMax(value = "100.0", message = "Pojemność nie może przekraczać 100")
    private Double capacity;

    @DecimalMin(value = "0.1", message = "Szerokość robocza musi być większa niż 0")
    @DecimalMax(value = "80.0", message = "Szerokość robocza nie może przekraczać 80")
    private Double workingWidth;

    @Size(max = 30, message = "Numer polisy nie może przekraczać 20 znaków")
    private String insurancePolicyNumber;

    @Future(message = "Data wygaśnięcia polisy musi być w przyszłości")
    private LocalDate insuranceExpirationDate;

    @Future(message = "Data wygaśnięcia przeglądu musi być w przyszłości")
    private LocalDate inspectionExpireDate;

    public AddUpdateFarmEquipmentRequest(Integer equipmentId, String equipmentName, String category, String brand, String model) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.category = category;
        this.brand = brand;
        this.model = model;
    }
}