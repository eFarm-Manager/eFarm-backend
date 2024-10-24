package com.efarm.efarmbackend.repository.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgriculturalRecordRepository extends JpaRepository<AgriculturalRecord, Integer> {

    List<AgriculturalRecord> findByLandparcelAndSeason(Landparcel landparcel, Season season);

    List<AgriculturalRecord> findByLandparcelAndSeasonAndCrop(Landparcel landparcel, Season season, Crop crop);
}
