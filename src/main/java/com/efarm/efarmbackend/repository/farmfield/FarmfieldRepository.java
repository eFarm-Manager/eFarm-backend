package com.efarm.efarmbackend.repository.farmfield;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.efarm.efarmbackend.model.farmfield.Farmfield;
import com.efarm.efarmbackend.model.farmfield.FarmfieldId;

import java.util.Optional;

@Repository
public interface FarmfieldRepository extends JpaRepository<Farmfield, FarmfieldId>  {

    @Query("SELECT MAX(fm.id.id) FROM Farmfield fm WHERE fm.id.farmId = :farmId")
    Optional<Integer> findMaxIdForFarm(@Param("farmId") Integer farmId);

    default Integer findNextFreeIdForFarm(Integer farmId) {
        return findMaxIdForFarm(farmId).map(maxId -> maxId + 1).orElse(1);
    }

}
