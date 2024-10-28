package com.efarm.efarmbackend.service.agriculturalrecords

import com.efarm.efarmbackend.model.agriculturalrecords.Season
import com.efarm.efarmbackend.repository.agriculturalrecords.SeasonRepository

import java.util.List

import spock.lang.Specification
import spock.lang.Subject

class SeasonServiceSpec extends Specification {

    def seasonRepository = Mock(SeasonRepository)

    @Subject
    SeasonService seasonService = new SeasonService(
        seasonRepository: seasonRepository
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
        Season season = Mock(Season)

        seasonRepository.findById(seasonService.getCurrentSeasonId()) >> Optional.of(season)

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

    // it will stop working after 2024
    def "should return correct season ID for now"() {
        when:
        Integer seasonId = seasonService.getCurrentSeasonId()

        then:
        seasonId == 3
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
