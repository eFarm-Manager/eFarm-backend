package com.efarm.efarmbackend.model.equipment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FarmEquipmentShortDTO {

    private Integer equipmentId;
    private String equipmentName;
    private String category;
    private String brand;
    private String model;

    public FarmEquipmentShortDTO(Integer equipmentId, String equipmentName, String category, String brand, String model) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.category = category;
        this.brand = brand;
        this.model = model;
    }
}
