package com.efarm.efarmbackend.model.equipment;

import com.efarm.efarmbackend.model.agroactivity.ActivityHasEquipment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentSummaryDTO {

    private Integer equipmentId;
    private String equipmentName;
    private String category;
    private String brand;
    private String model;

    public EquipmentSummaryDTO(FarmEquipment equipment) {
        this.equipmentId = equipment.getId().getId();
        this.equipmentName = equipment.getEquipmentName();
        this.category = equipment.getCategory().getCategoryName();
        this.brand = equipment.getBrand();
        this.model = equipment.getModel();
    }

    public EquipmentSummaryDTO(ActivityHasEquipment activityHasEquipment) {
        this.equipmentId = activityHasEquipment.getFarmEquipment().getId().getId();
        this.equipmentName = activityHasEquipment.getFarmEquipment().getEquipmentName();
        this.category = activityHasEquipment.getFarmEquipment().getCategory().getCategoryName();
        this.brand = activityHasEquipment.getFarmEquipment().getBrand();
        this.model = activityHasEquipment.getFarmEquipment().getModel();
    }
}