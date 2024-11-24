package com.efarm.efarmbackend.service.agriculturalrecords

import com.efarm.efarmbackend.model.agriculturalrecords.Season
import com.efarm.efarmbackend.repository.agriculturalrecords.SeasonRepository

import java.util.List
import java.time.Clock;
import spock.lang.Specification
import spock.lang.Subject

class SeasonServiceSpec extends Specification {

    def seasonRepository = Mock(SeasonRepository)

    @Subject
    SeasonService seasonService = new SeasonService(
        seasonRepository
    )

    def "should get Season by name"() {
        given:
        Season season = new Season(name: '2024/2025')
        seasonRepository.findByName('2024/2025') >> season

        when:
        Season result = seasonService.getSeasonByName('2024/2025')

        then:
        result == season
    }

    def "should not get Season by name"() {
        given:
        seasonRepository.findByName('2024/2025') >> null

        when:
        Season result = seasonService.getSeasonByName('2024/2025')

        then:
        result == null
    }

    def "should get current season"() {
        given:
        Clock clock = Clock.fixed(java.time.Instant.parse('2024-10-01T00:00:00Z'), java.time.ZoneId.systemDefault())

        Season season = Mock(Season)

        seasonRepository.findById(seasonService.getCurrentSeasonId(clock)) >> Optional.of(season)

        when:
        Season result = seasonService.getCurrentSeason()

        then:
        result == season
    }

    def "should get all seasons"() {
        given:
        List<Season> seasons = [
            new Season(name: '2024/2025'),
            new Season(name: '2025/2026')
        ]
        seasonRepository.findAll() >> seasons

        when:
        List<String> result = seasonService.getAvailableSeasons()

        then:
        result == ['2024/2025', '2025/2026']
    }

    def "should return correct season ID for 10 2024"() {
        given:
        Clock clock = Clock.fixed(java.time.Instant.parse('2024-10-01T00:00:00Z'), java.time.ZoneId.systemDefault())

        when:
        Integer seasonId = seasonService.getCurrentSeasonId(clock)

        then:
        seasonId == 3
    }

    def "should return correct season ID for 5 2024"() {
        given:
        Clock clock = Clock.fixed(java.time.Instant.parse('2024-05-01T00:00:00Z'), java.time.ZoneId.systemDefault())

        when:
        Integer seasonId = seasonService.getCurrentSeasonId(clock)

        then:
        seasonId == 2
    }

    def "should calculate season ID based on starting year"() {
        given:
        int year = 2024

        when:
        Integer seasonId = seasonService.calculateSeasonId(year)

        then:
        seasonId == 3
    }

    def "should calculate season ID for base year"() {
        given:
        int year = 2023

        when:
        Integer seasonId = seasonService.calculateSeasonId(year)

        then:
        seasonId == 2
    }

}
