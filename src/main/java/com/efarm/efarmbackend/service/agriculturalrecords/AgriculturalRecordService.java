package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.CreateNewAgriculturalRecordRequest;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.UpdateAgriculturalRecordRequest;
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository;
import com.efarm.efarmbackend.repository.agriculturalrecords.CropRepository;
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgriculturalRecordService {

    @Autowired
    private AgriculturalRecordRepository agriculturalRecordRepository;

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private LandparcelRepository landparcelRepository;

    @Autowired
    private UserService userService;

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
        double maxAvailableArea = landparcel.getArea() - totalUsedArea;

        if (totalUsedArea + recordRequest.getArea() > landparcel.getArea()) {
            double roundedDownArea = Math.floor(maxAvailableArea * 100) / 100;
            throw new Exception("Maksymalna niewykorzystana powierzchnia na tym polu to: " + roundedDownArea + " ha. Spróbuj najpierw zmniejszyć powierzchnię pozostałych upraw.");
        }
    }

    @Transactional
    public void updateAgriculturalRecord(Integer id, UpdateAgriculturalRecordRequest updateRequest) throws Exception {

        Farm loggedUserFarm = userService.getLoggedUserFarm();
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(id, loggedUserFarm.getId());
        AgriculturalRecord recordToUpdate = agriculturalRecordRepository.findById(agriculturalRecordId)
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
        if (updateRequest.getDescription() != null) {
            recordToUpdate.setDescription(updateRequest.getDescription());
        }
        agriculturalRecordRepository.save(recordToUpdate);
    }

    public void validateUpdatedCropArea(Landparcel landparcel, Season season, UpdateAgriculturalRecordRequest updateRequest, AgriculturalRecord recordToUpdate) throws Exception {
        List<AgriculturalRecord> existingRecords = agriculturalRecordRepository.findByLandparcelAndSeason(landparcel, season);

        double totalUsedArea = existingRecords.stream()
                .filter(record -> !record.getId().equals(recordToUpdate.getId()))
                .mapToDouble(AgriculturalRecord::getArea)
                .sum();

        double maxAvailableArea = landparcel.getArea() - totalUsedArea;

        if (totalUsedArea + updateRequest.getArea() > landparcel.getArea()) {
            double roundedDownArea = Math.floor(maxAvailableArea * 100) / 100;
            throw new Exception("Maksymalna niewykorzystana powierzchnia na tym polu to: " + roundedDownArea + " ha. Spróbuj najpierw zmniejszyć powierzchnię pozostałych upraw.");
        }
    }

    public void createAgriculturalRecordForLandparcel(Landparcel landparcel, Farm loggedUserFarm, Season season) throws Exception {
        Crop currentCrop = cropRepository.findByName("uprawa nieoznaczona");

        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(
                agriculturalRecordRepository.findNextFreeIdForFarm(loggedUserFarm.getId()),
                loggedUserFarm.getId()
        );
        AgriculturalRecord agriculturalRecord = new AgriculturalRecord(
                agriculturalRecordId,
                season,
                landparcel,
                currentCrop,
                landparcel.getArea(),
                loggedUserFarm,
                null
        );
        agriculturalRecordRepository.save(agriculturalRecord);
    }

    public void deleteAgriculturalRecord(Integer id) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(id, loggedUserFarm.getId());
        //TODO: w przyszłości tutaj należy dorobić usuwanie wszystkich powiązanych zabiegów
        if (agriculturalRecordRepository.existsById(agriculturalRecordId)) {
            agriculturalRecordRepository.deleteById(agriculturalRecordId);
        } else {
            throw new Exception("Ewidencja, którą próbujesz usunąć nie istnieje!");
        }
    }
}