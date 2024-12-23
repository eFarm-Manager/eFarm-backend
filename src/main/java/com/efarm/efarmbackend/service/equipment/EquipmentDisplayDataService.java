package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import jakarta.annotation.PostConstruct;

import java.util.List;

public interface EquipmentDisplayDataService {
    @PostConstruct
    void initializeCache();

    List<String> getFieldsForCategory(String categoryName);

    List<EquipmentCategoryDTO> getAllCategoriesWithFields();
}