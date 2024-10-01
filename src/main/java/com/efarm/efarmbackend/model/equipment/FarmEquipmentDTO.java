package com.efarm.efarmbackend.model.equipment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FarmEquipmentDTO {
    private Integer equipmentId;
    private String equipmentName;
    private String category;
    private String brand;
    private String model;
    private Integer power;
    private Double capacity;
    private Double workingWidth;
    private Long insurancePolicyNumber;
    private LocalDate insuranceExpirationDate;
    private LocalDate inspectionExpireDate;

    public FarmEquipmentDTO(Integer equipmentId, String equipmentName, String category, String brand, String model) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.category = category;
        this.brand = brand;
        this.model = model;
    }
}

