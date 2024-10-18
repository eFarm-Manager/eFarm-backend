package com.efarm.efarmbackend.repository.landparcel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.efarm.efarmbackend.model.landparcel.LandparcelHasFarmfield;

@Repository
public interface LandparcelHasFarmfieldRepository extends JpaRepository<LandparcelHasFarmfield, Integer> {
    
}
