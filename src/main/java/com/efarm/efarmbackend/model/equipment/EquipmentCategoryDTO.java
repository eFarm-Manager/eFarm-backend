package com.efarm.efarmbackend.model.equipment;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EquipmentCategoryDTO {
    private String categoryName;
    private List<String> fields;

    public EquipmentCategoryDTO(String categoryName, List<String> fields) {
        this.categoryName = categoryName;
        this.fields = fields != null ? new ArrayList<>(fields) : null;
    }

    public List<String> getFields() {
        return fields != null ? new ArrayList<>(fields) : null;
    }

    public void setFields(List<String> fields) {
        this.fields = fields != null ? new ArrayList<>(fields) : null;
    }
}

