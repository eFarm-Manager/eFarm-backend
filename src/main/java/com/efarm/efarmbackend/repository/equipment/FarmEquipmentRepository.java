package com.efarm.efarmbackend.repository.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmEquipmentRepository extends JpaRepository<FarmEquipment, FarmEquipmentId> {
    List<FarmEquipment> findByFarmIdFarm_Id(Integer farmId);
}
