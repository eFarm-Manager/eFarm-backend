package com.efarm.efarmbackend.model.equipment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class EquipmentCategoryDTO {
    private String categoryName;
    private List<String> fields;
}

