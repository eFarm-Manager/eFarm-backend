package com.efarm.efarmbackend.repository.landparcel;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LandparcelRepository extends JpaRepository<Landparcel, LandparcelId> {
    List<Landparcel> findByFarmId(Integer farmId);

    Boolean existsByGeodesyLandparcelNumberAndFarm(String geodesyLandparcelNumber, Farm farm);

    Boolean existsByFarmAndName(Farm farm, String name);

    List<Landparcel> findByFarmIdAndIsAvailableTrue(Integer farmId);

    @Query("SELECT MAX(l.id.id) FROM Landparcel l WHERE l.id.farmId = :farmId")
    Optional<Integer> findMaxIdForFarm(@Param("farmId") Integer farmId);

    default Integer findNextFreeIdForFarm(Integer farmId) {
        return findMaxIdForFarm(farmId).map(maxId -> maxId + 1).orElse(1);
    }
}
