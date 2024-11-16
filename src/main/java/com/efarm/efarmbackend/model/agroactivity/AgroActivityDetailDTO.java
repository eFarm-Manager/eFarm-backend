package com.efarm.efarmbackend.model.agroactivity;

import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO;
import com.efarm.efarmbackend.model.landparcel.LandparcelSummaryDTO;
import com.efarm.efarmbackend.model.user.UserSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AgroActivityDetailDTO {
    private Integer id;
    private String name;
    private Instant date;
    private Boolean isCompleted;
    private String categoryName;
    private String usedSubstances;
    private String appliedDose;
    private String description;
    private Double area;
    private LandparcelSummaryDTO landparcel;
    private List<UserSummaryDTO> operators;
    private List<EquipmentSummaryDTO> equipment;

    public AgroActivityDetailDTO(AgroActivity agroActivity, LandparcelSummaryDTO landparcelSummaryDTO, List<UserSummaryDTO> operators, List<EquipmentSummaryDTO> equipment) {
        this.id = agroActivity.getId().getId();
        this.name = agroActivity.getName();
        this.date = agroActivity.getDate();
        this.isCompleted = agroActivity.getIsCompleted();
        this.categoryName = agroActivity.getActivityCategory().getName();
        this.usedSubstances = agroActivity.getUsedSubstances();
        this.appliedDose = agroActivity.getAppliedDose();
        this.description = agroActivity.getDescription();
        this.area = agroActivity.getAgriculturalRecord().getArea();
        this.landparcel = landparcelSummaryDTO;
        this.operators = operators;
        this.equipment = equipment;
    }
}

