package com.efarm.efarmbackend.repository.agroactivity;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgroActivityRepository extends JpaRepository<AgroActivity, AgroActivityId> {

    List<AgroActivity> findByAgriculturalRecordId(AgriculturalRecordId agriculturalRecordId);

    List<AgroActivity> findByAgriculturalRecord(AgriculturalRecord agriculturalRecord);

    @Query("SELECT a FROM AgroActivity a " +
            "LEFT JOIN FETCH a.agriculturalRecord l " +
            "LEFT JOIN FETCH a.activityCategory " +
            "WHERE a.id = :id")
    Optional<AgroActivity> findWithDetailsById(@Param("id") AgroActivityId id);

    @Query("SELECT MAX(aa.id.id) FROM AgroActivity aa WHERE aa.farm.id = :farmId")
    Optional<Integer> findMaxIdForFarm(@Param("farmId") Integer farmId);

    default Integer findNextFreeIdForFarm(Integer farmId) {
        return findMaxIdForFarm(farmId).map(maxId -> maxId + 1).orElse(1);
    }
}
