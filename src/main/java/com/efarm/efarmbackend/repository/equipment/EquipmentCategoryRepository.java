package com.efarm.efarmbackend.repository.equipment;

import com.efarm.efarmbackend.model.equipment.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, Integer> {
    @Query("SELECT e.categoryName FROM EquipmentCategory e")
    List<String> findAllCategoryNames();
}

