package com.efarm.efarmbackend.repository.farm;

import com.efarm.efarmbackend.model.farm.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Integer> {

    boolean existsByFarmName(String farmName);

    List<Farm> findByIsActiveTrue();

    List<Farm> findByIsActiveFalse();
}