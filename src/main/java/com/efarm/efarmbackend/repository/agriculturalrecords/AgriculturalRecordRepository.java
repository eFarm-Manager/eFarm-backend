package com.efarm.efarmbackend.repository.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgriculturalRecordRepository extends JpaRepository<AgriculturalRecord, AgriculturalRecordId> {

    List<AgriculturalRecord> findByLandparcelAndSeason(Landparcel landparcel, Season season);

    List<AgriculturalRecord> findByLandparcelAndSeasonAndCrop(Landparcel landparcel, Season season, Crop crop);

    @Query("SELECT ar.crop.name, SUM(ar.area) " +
            "FROM AgriculturalRecord ar " +
            "WHERE ar.season.id = :seasonId AND ar.landparcel.farm.id = :farmId " +
            "GROUP BY ar.crop.name")
    List<Object[]> findCropStatisticsBySeasonAndFarm(@Param("seasonId") Integer seasonId, @Param("farmId") Integer farmId);

    @Query("SELECT MAX(ar.id.id) FROM AgriculturalRecord ar WHERE ar.farm.id = :farmId")
    Optional<Integer> findMaxIdForFarm(@Param("farmId") Integer farmId);

    default Integer findNextFreeIdForFarm(Integer farmId) {
        return findMaxIdForFarm(farmId).map(maxId -> maxId + 1).orElse(1);
    }
}