package com.efarm.efarmbackend.repository.equipment;

import com.efarm.efarmbackend.model.equipment.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, Integer> {

    @Query("SELECT e.categoryName FROM EquipmentCategory e")
    List<String> findAllCategoryNames();

    EquipmentCategory findByCategoryName(String category);
}