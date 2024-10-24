package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.CreateNewAgriculturalRecordRequest;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.UpdateAgriculturalRecordRequest;
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository;
import com.efarm.efarmbackend.repository.agriculturalrecords.CropRepository;
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.round;

@Service
public class AgriculturalRecordService {

    @Autowired
    private SeasonService seasonService;

    @Autowired
    private AgriculturalRecordRepository agriculturalRecordRepository;

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private LandparcelRepository landparcelRepository;

    public void createInitialAgriculturalRecordForLandparcel(Landparcel landparcel) throws Exception {

        Season currentSeason = seasonService.getCurrentSeason();
        Crop currentCrop = cropRepository.findByName("uprawa nieoznaczona");

        AgriculturalRecord agriculturalRecord = new AgriculturalRecord(currentSeason, landparcel, currentCrop, landparcel.getArea());
        agriculturalRecordRepository.save(agriculturalRecord);
    }

    public List<AgriculturalRecord> filterRecordsBySearchQuery(List<AgriculturalRecord> agriculturalRecords, String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            agriculturalRecords = agriculturalRecords.stream()
                    .filter(record -> record.getLandparcel().getName().contains(searchQuery) ||
                            record.getCrop().getName().contains(searchQuery))
                    .toList();
        }
        return agriculturalRecords;
    }

    public List<AgriculturalRecord> getAgriculturalRecordsForFarmAndSeason(Integer farmId, Season season) {
        return landparcelRepository.findByFarmId(farmId).stream()
                .filter(Landparcel::getIsAvailable)
                .flatMap(landparcel -> agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season).stream())
                .collect(Collectors.toList());
    }

    public Crop validateCrop(Landparcel landparcel, Season season, String cropName) throws Exception {
        Crop crop = cropRepository.findByName(cropName);
        if (crop == null) {
            throw new Exception("Wybrano nieprawidłowy rodzaj uprawy");
        }
        List<AgriculturalRecord> cropsOnLandparcel = agriculturalRecordRepository.findByLandparcelAndSeasonAndCrop(landparcel, season, crop);
        if (!cropsOnLandparcel.isEmpty()) {
            throw new Exception("Wybrana uprawa już istnieje na tym polu. Możesz zmienić jej powierzchnię zamiast dodawać ją ponownie.");
        }
        return crop;
    }

    public void validateCropArea(Landparcel landparcel, Season season, CreateNewAgriculturalRecordRequest recordRequest) throws Exception {
        List<AgriculturalRecord> existingRecords = agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season);
        double totalUsedArea = existingRecords.stream().mapToDouble(AgriculturalRecord::getArea).sum();

        if (totalUsedArea + recordRequest.getArea() > landparcel.getArea()) {
            double areaExceeded = (landparcel.getArea() - recordRequest.getArea() - totalUsedArea) * (-1.0);
            throw new Exception("Wprowadzona łączna powierzchnia upraw na tym polu została przekroczona o: " + round(areaExceeded, 2) + "ha. Spróbuj najpierw ustawić powierzchnię pozostałych upraw.");
        }
    }

    public void updateAgriculturalRecord(Integer id, UpdateAgriculturalRecordRequest updateRequest) throws Exception {

        AgriculturalRecord recordToUpdate = agriculturalRecordRepository.findById(id)
                .orElseThrow(() -> new Exception("Nie znaleziono ewidencji"));

        Landparcel landparcel = recordToUpdate.getLandparcel();
        Season season = recordToUpdate.getSeason();

        if (updateRequest.getCropName() != null) {
            Crop newCrop = validateCrop(landparcel, season, updateRequest.getCropName());
            recordToUpdate.setCrop(newCrop);
        }
        if (updateRequest.getArea() != null) {
            validateUpdatedCropArea(landparcel, season, updateRequest, recordToUpdate);
            recordToUpdate.setArea(updateRequest.getArea());
        }
        agriculturalRecordRepository.save(recordToUpdate);
    }

    public void validateUpdatedCropArea(Landparcel landparcel, Season season, UpdateAgriculturalRecordRequest updateRequest, AgriculturalRecord recordToUpdate) throws Exception {
        List<AgriculturalRecord> existingRecords = agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season);

        double totalUsedArea = existingRecords.stream()
                .filter(record -> !record.getId().equals(recordToUpdate.getId()))
                .mapToDouble(AgriculturalRecord::getArea)
                .sum();

        if (totalUsedArea + updateRequest.getArea() > landparcel.getArea()) {
            double areaExceeded = (landparcel.getArea() - updateRequest.getArea() - totalUsedArea) * (-1.0);
            throw new Exception("Wprowadzona łączna powierzchnia upraw na tym polu została przekroczona o: " + round(areaExceeded, 2) + "ha. Spróbuj najpierw ustawić powierzchnię pozostałych upraw.");
        }
    }
}
