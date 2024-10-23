package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository;
import com.efarm.efarmbackend.repository.agriculturalrecords.CropRepository;
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<AgriculturalRecord> filterRecordsBySearchQuery(List<AgriculturalRecord> agriculturalRecords,  String searchQuery) {
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
}
