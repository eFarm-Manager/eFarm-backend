package com.efarm.efarmbackend.repository.landparcel;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus;
import com.efarm.efarmbackend.model.landparcel.LandOwnershipStatus;

import jakarta.transaction.Transactional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class LandOwnershipStatusRepositoryIT {
    @Autowired
    LandOwnershipStatusRepository landOwnershipStatusRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("tests that status owned is being found")
    public void shouldFindByOwnershipStatus() {
        // When
        Optional<LandOwnershipStatus> foundStatus = landOwnershipStatusRepository.findByOwnershipStatus(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED);

        // Then
        assertThat(foundStatus.get(), notNullValue());
        assertThat(foundStatus.get().getOwnershipStatus(),is(ELandOwnershipStatus.STATUS_PRIVATELY_OWNED));
    }

    @Test
    @DisplayName("tests that status leased is being found")
    public void shouldFindByLeasedStatus() {
        // When
        Optional<LandOwnershipStatus> foundStatus = landOwnershipStatusRepository.findByOwnershipStatus(ELandOwnershipStatus.STATUS_LEASE);
        // Then
        assertThat(foundStatus.get(), notNullValue());
        assertThat(foundStatus.get().getOwnershipStatus(),is(ELandOwnershipStatus.STATUS_LEASE));   
    }
}
