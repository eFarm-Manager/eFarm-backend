package com.efarm.efarmbackend.model.equipment;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(makeFinal = true)
public class EquipmentCategoryDTO {
    private String categoryName;
    private List<String> fields;

    public EquipmentCategoryDTO(String categoryName, List<String> fields) {
        this.categoryName = categoryName;
        this.fields = fields != null ? new ArrayList<>(fields) : null;
    }
}

