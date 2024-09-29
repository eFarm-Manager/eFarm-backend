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
    private String equipmentName;
    private String category;
    private Boolean isAvailable;
    private String brand;
    private Integer power;
    private Double capacity;
    private Double workingWidth;
    private Long insurancePolicyNumber;
    private LocalDate insuranceExpirationDate;
    private LocalDate inspectionExpireDate;
}

