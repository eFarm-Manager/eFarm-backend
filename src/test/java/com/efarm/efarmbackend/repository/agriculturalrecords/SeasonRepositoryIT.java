package com.efarm.efarmbackend.repository.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class SeasonRepositoryIT {
    @Autowired
    SeasonRepository seasonRepository;

    @Test
    public void testFindByName() {
        // Given
        String seasonName = "2024/2025";

        // When
        Season foundSeason = seasonRepository.findByName(seasonName);

        // Then
        assertThat(foundSeason, notNullValue());
        assertThat(foundSeason.getName(), is(seasonName));
    }

    @Test
    public void testDoesNotFindByNameIfDoesntExist() {
        // Given
        String seasonName = "not found season";

        // When
        Season foundSeason = seasonRepository.findByName(seasonName);

        // Then
        assertThat(foundSeason, nullValue());
    }
}
