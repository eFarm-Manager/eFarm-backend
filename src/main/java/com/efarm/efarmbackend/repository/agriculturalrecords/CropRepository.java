package com.efarm.efarmbackend.repository.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CropRepository extends JpaRepository<Crop, Integer> {

    Crop findByName(String name);
}