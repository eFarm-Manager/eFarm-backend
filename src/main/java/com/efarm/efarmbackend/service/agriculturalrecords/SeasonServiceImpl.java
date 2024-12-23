package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.repository.agriculturalrecords.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;

    @Override
    public Season getSeasonByName(String name) {
        return seasonRepository.findByName(name);
    }

    @Override
    public Season getCurrentSeason() throws Exception {
        return seasonRepository.findById(getCurrentSeasonId(Clock.systemDefaultZone()))
                .orElseThrow(() -> new Exception("Nie można automatycznie ustawić obecnego sezonu uprawy"));
    }

    @Override
    public List<String> getAvailableSeasons() {
        return seasonRepository.findAll().stream()
                .map(Season::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getCurrentSeasonId(Clock clock) {
        LocalDate currentDate = LocalDate.now(clock);
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();

        if (month >= 8) {
            return calculateSeasonId(year);
        } else {
            return calculateSeasonId(year - 1);
        }
    }
    private Integer calculateSeasonId(int startingYear) {
        int baseYear = 2023;
        int baseId = 2;
        return baseId + (startingYear - baseYear);
    }
}