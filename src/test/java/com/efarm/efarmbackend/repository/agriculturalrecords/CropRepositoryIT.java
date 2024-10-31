package com.efarm.efarmbackend.repository.agriculturalrecords;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.efarm.efarmbackend.model.agriculturalrecords.Crop;

import jakarta.transaction.Transactional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class CropRepositoryIT {
    @Autowired
    CropRepository cropRepository;

    @Test
    public void testFindByCropName() {
        // Given
        String cropName = "ziemniak";

        // When
        Crop foundCrop = cropRepository.findByName(cropName);

        // Then
        assertThat(foundCrop, notNullValue());
        assertThat(foundCrop.getName(), is(cropName));
    }

    @Test
    public void testDoesNotFindByCropNameIfDoesntExist() {
        // Given
        String cropName = "not found crop";

        // When
        Crop foundCrop = cropRepository.findByName(cropName);

        // Then
        assertThat(foundCrop, nullValue());
    }
    
}
