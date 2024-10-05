package com.efarm.efarmbackend.repository.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmEquipmentRepository extends JpaRepository<FarmEquipment, FarmEquipmentId> {
    List<FarmEquipment> findByFarmIdFarm_Id(Integer farmId);

    @Query("SELECT MAX(f.id.id) FROM FarmEquipment f")
    Optional<Integer> findMaxId();

    default Integer findNextFreeId() {
        return findMaxId().map(maxId -> maxId + 1).orElse(1);
    }
}
