package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordDTO;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.service.user.UserService;
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
}
