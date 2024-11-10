package com.efarm.efarmbackend.repository.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.farm.Farm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmEquipmentRepository extends JpaRepository<FarmEquipment, FarmEquipmentId> {

    List<FarmEquipment> findByFarmIdFarm_Id(Integer farmId);

    Boolean existsByEquipmentNameAndFarmIdFarm(@Size(max = 60) @NotNull String equipmentName, Farm farmIdFarm);

    @Query("SELECT MAX(f.id.id) FROM FarmEquipment f WHERE f.farmIdFarm.id = :farmId")
    Optional<Integer> findMaxIdForFarm(@Param("farmId") Integer farmId);

    default Integer findNextFreeIdForFarm(Integer farmId) {
        return findMaxIdForFarm(farmId).map(maxId -> maxId + 1).orElse(1);
    }
}