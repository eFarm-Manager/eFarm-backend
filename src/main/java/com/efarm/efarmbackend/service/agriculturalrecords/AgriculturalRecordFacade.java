package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordDTO;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.CreateNewAgriculturalRecordRequest;
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository;
import com.efarm.efarmbackend.service.landparcel.LandparcelService;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgriculturalRecordFacade {

    @Autowired
    private SeasonService seasonService;

    @Autowired
    private UserService userService;

    @Autowired
    private AgriculturalRecordService agriculturalRecordService;

    @Autowired
    private AgriculturalRecordRepository agriculturalRecordRepository;

    @Autowired
    private LandparcelService landparcelService;


    public List<AgriculturalRecordDTO> getAgriculturalRecords(String seasonName, String searchQuery) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();

        Season season = (seasonName == null || seasonName.isEmpty())
                ? seasonService.getCurrentSeason()
                : seasonService.getSeasonByName(seasonName);

        List<AgriculturalRecord> agriculturalRecords = agriculturalRecordService.getAgriculturalRecordsForFarmAndSeason(loggedUserFarm.getId(), season);
        agriculturalRecords = agriculturalRecordService.filterRecordsBySearchQuery(agriculturalRecords, searchQuery);

        return agriculturalRecords.stream()
                .map(record -> new AgriculturalRecordDTO(record.getId(), record.getLandparcel().getName(), record.getCrop().getName(), record.getArea()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addAgriculturalRecord(CreateNewAgriculturalRecordRequest recordRequest) throws Exception {

        Farm loggedUserFarm = userService.getLoggedUserFarm();
        Season season = (recordRequest.getSeason() == null || recordRequest.getSeason().isEmpty())
                ? seasonService.getCurrentSeason()
                : seasonService.getSeasonByName(recordRequest.getSeason());

        Landparcel landparcel = landparcelService.findlandparcelByFarm(recordRequest.getLandparcelId(), loggedUserFarm);

        if (!landparcel.getIsAvailable()) {
            throw new Exception("Wybrane pole jest niedostępne!");
        }

        agriculturalRecordService.validateCropArea(landparcel, season, recordRequest);
        Crop crop = agriculturalRecordService.validateCrop(landparcel, season, recordRequest.getCropName());
        AgriculturalRecord newRecord = new AgriculturalRecord(season, landparcel, crop, recordRequest.getArea());
        agriculturalRecordRepository.save(newRecord);
    }
}
