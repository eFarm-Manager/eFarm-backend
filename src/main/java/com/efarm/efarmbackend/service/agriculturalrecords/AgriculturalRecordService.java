package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.CreateNewAgriculturalRecordRequest;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.UpdateAgriculturalRecordRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface AgriculturalRecordService {
    List<AgriculturalRecord> filterRecordsBySearchQuery(List<AgriculturalRecord> agriculturalRecords, String searchQuery);

    List<AgriculturalRecord> getAgriculturalRecordsForFarmAndSeason(Integer farmId, Season season);

    Crop validateCrop(Landparcel landparcel, Season season, String cropName, Boolean showAdditionalExceptionInfo) throws Exception;

    void validateCropArea(Landparcel landparcel, Season season, CreateNewAgriculturalRecordRequest recordRequest) throws Exception;

    AgriculturalRecord findAgriculturalRecordById(Integer id, Integer loggedUserFarmId) throws RuntimeException;

    @Transactional
    void updateAgriculturalRecord(Integer id, UpdateAgriculturalRecordRequest updateRequest) throws Exception;

    void validateUpdatedCropArea(Landparcel landparcel, Season season, UpdateAgriculturalRecordRequest updateRequest, AgriculturalRecord recordToUpdate) throws Exception;

    void createAgriculturalRecordForLandparcel(Landparcel landparcel, Farm loggedUserFarm, Season season);

    @Transactional
    void deleteAgriculturalRecord(Integer id) throws Exception;

    @Transactional
    void deleteAllAgriculturalRecordsForFarm(Farm farm) throws Exception;
}
