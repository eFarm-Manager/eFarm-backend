package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.Season;

import java.time.Clock;
import java.util.List;

public interface SeasonService {
    Season getSeasonByName(String name);

    Season getCurrentSeason() throws Exception;

    List<String> getAvailableSeasons();

    Integer getCurrentSeasonId(Clock clock);

    default Integer calculateSeasonId(int startingYear) {
        int baseYear = 2023;
        int baseId = 2;
        return baseId + (startingYear - baseYear);
    }
}
