package com.efarm.efarmbackend.repository.farm;

import com.efarm.efarmbackend.model.farm.Farm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class FarmRepositoryIT {
    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Tests if existing farm does exist by name")
    public void testExistsByFarmName() {
        // Given
        Farm farm = entityManager.find(Farm.class, 1);
        String farmNameTest = farm.getFarmName();

        // When
        Boolean existsFarmByName = farmRepository.existsByFarmName(farmNameTest);

        // Then
        assertThat(existsFarmByName, is(true));
    }

    @Test
    @DisplayName("Tests if non existing farm exists by name")
    public void testDoesntFindNotExistingCode() {
        // Given
        String farmNameTest = "nonExistingFarmName";

        // When
        Boolean existsFarmByName = farmRepository.existsByFarmName(farmNameTest);

        // Then
        assertThat(existsFarmByName, is(false));
    }

    @Test
    @DisplayName("Tests that all active farms are collected")
    public void testFindActiveFarms() {
        //given
        Long countActive = entityManager.getEntityManager()
                .createQuery("SELECT COUNT(f) FROM Farm f WHERE f.isActive = true", Long.class)
                .getSingleResult();

        // when
        List<Farm> activeFarms = farmRepository.findByIsActive(true);

        // then
        assertThat(activeFarms, not(empty()));
        assertThat(countActive.intValue(), is(activeFarms.size()));
        assertThat(activeFarms, everyItem(hasProperty("isActive", is(true))));
    }

    @Test
    public void testFindActiveFalse() {
        //given
        Long countInactive = entityManager.getEntityManager()
                .createQuery("SELECT COUNT(f) FROM Farm f WHERE f.isActive = false", Long.class)
                .getSingleResult();

        // when
        List<Farm> nonActiveFarms = farmRepository.findByIsActive(false);

        // then
        assertThat(nonActiveFarms, not(empty()));
        assertThat(countInactive.intValue(), is(nonActiveFarms.size()));
        assertThat(nonActiveFarms, everyItem(hasProperty("isActive", is(false))));
    }
}