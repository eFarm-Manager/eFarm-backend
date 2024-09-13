package com.efarm.efarmbackend.repository.farm;

import com.efarm.efarmbackend.model.farm.Farm;

import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
		assertThat(existsFarmByName,is(true));
	}
	@Test
    @DisplayName("Tests if non existing farm exists by name")
    public void testDoesntFindNotExistingCode() {
        // Given
		String farmNameTest = "nonExistingFarmName";

        // When
        Boolean existsFarmByName = farmRepository.existsByFarmName(farmNameTest);

        // Then
        assertThat(existsFarmByName,is(false));
    }
}