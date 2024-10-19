package com.efarm.efarmbackend.repository.farm;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.efarm.efarmbackend.model.farm.ActivationCode;

import jakarta.transaction.Transactional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class ActivationCodeRepositoryIT {
    @Autowired
    ActivationCodeRepository activationCodeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Tests finding activation code by code name")
    public void testFindByCode() {
        // Given
        ActivationCode activationCode = entityManager.find(ActivationCode.class, 1);
        String codeTest = activationCode.getCode();

        // When
        Optional<ActivationCode> foundCode = activationCodeRepository.findByCode(codeTest);

        // Then
        assertThat(foundCode.isPresent(), is(true));
        assertThat(foundCode.get(), notNullValue());
        assertThat(foundCode.get().getCode(), is(codeTest));
    }

    @Test
    @DisplayName("Tests finding not existing code")
    public void testDoesntFindNotExistingCode() {
        // Given
        String codeTest = "upsieNonExistingCode";

        // When
        Optional<ActivationCode> foundCode = activationCodeRepository.findByCode(codeTest);

        // Then
        assertThat(foundCode.isPresent(), is(false));
    }

    @Test
    public void testFindById() {
        // Given
        ActivationCode activationCode = entityManager.find(ActivationCode.class, 1);

        // When
        ActivationCode foundCode = activationCodeRepository.findById(1);

        // Then
        assertThat(foundCode, notNullValue());
        assertThat(foundCode.getCode(), is(activationCode.getCode()));
    }

    @Test
    public void testDoesntFindByIdThatNotExist() {
        // When
        ActivationCode foundCode = activationCodeRepository.findById(9999);

        // Then
        assertThat(foundCode, is(nullValue()));
    }
}
