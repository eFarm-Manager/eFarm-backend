package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.repository.agriculturalrecords.SeasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeasonService {

    @Autowired
    private SeasonRepository seasonRepository;

    public Season getSeasonByName(String name) {
        return seasonRepository.findByName(name);
    }

    public Season getCurrentSeason() throws Exception {
        return seasonRepository.findById(getCurrentSeasonId())
                .orElseThrow(() -> new Exception("Nie można automatycznie ustawić obecnego sezonu uprawy"));
    }

    public List<String> getAvailableSeasons(){
        return seasonRepository.findAll().stream()
                .map(Season::getName)
                .collect(Collectors.toList());
    }

    public Integer getCurrentSeasonId() {
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();

        if (month >= 7) {
            return calculateSeasonId(year);
        } else {
            return calculateSeasonId(year - 1);
        }
    }

    private Integer calculateSeasonId(int startingYear) {
        int baseYear = 2023;  // basePoint, season 07.2023-06.2024 with id=2
        int baseId = 2;

        return baseId + (startingYear - baseYear);
    }
}
