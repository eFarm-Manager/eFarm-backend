package com.efarm.efarmbackend.repository.landparcel;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.NotNull;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus;
import com.efarm.efarmbackend.model.landparcel.LandOwnershipStatus;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class LandparcelRepositoryIT {
    @Autowired
    LandparcelRepository landparcelRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void shouldReturnLandparcelsForGivenFarmId() {
        //given
        Integer farmId = 1;
        Long countActive = entityManager.getEntityManager()
                .createQuery("SELECT COUNT(l) FROM Landparcel l WHERE l.id.farmId = 1", Long.class)
                .getSingleResult();

        // When
        List<Landparcel> result = landparcelRepository.findByFarmId(farmId);

        // Then
        assertThat(result.size(), is(countActive.intValue()));
        assertThat(result.get(0).getId().getFarmId(),is(farmId));
        assertThat(result.get(1).getId().getFarmId(),is(farmId));
    }

    @Test
    public void shouldReturnEmptyListForUnknownFarmId() {
        // When
        List<Landparcel> result = landparcelRepository.findByFarmId(999); 

        // Then
        assertThat(result.size(),is(0));
    }

    @Test
    public void shouldReturnTrueIfLandparcelExistsWithGivenParameters() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        Landparcel existingLandparcel = entityManager.getEntityManager()
            .createQuery("SELECT l FROM Landparcel l WHERE l.id.farmId = 1 AND l.id.id = 1", Landparcel.class)
            .getSingleResult();

        String district = existingLandparcel.getDistrict();
        String commune= existingLandparcel.getCommune();
        String geodesyRegistrationDistrictNumber = existingLandparcel.getGeodesyRegistrationDistrictNumber();
        String landparcelNumber = existingLandparcel.getLandparcelNumber();

        // When
        Boolean exists = landparcelRepository.existsByDistrictAndCommuneAndAndGeodesyRegistrationDistrictNumberAndLandparcelNumberAndFarm(
                district, commune, geodesyRegistrationDistrictNumber, landparcelNumber, farm);

        // Then
        assertThat(exists,is(true));
    }

    @Test
    public void shouldReturnFalseIfLandparcelDoesNotExistWithGivenParameters() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        
        // When
        Boolean exists = landparcelRepository.existsByDistrictAndCommuneAndAndGeodesyRegistrationDistrictNumberAndLandparcelNumberAndFarm(
                "DistrictUnknown", "CommuneUnknown", "GRDUnknown", "LPUnknown", farm);

        // Then
        assertThat(exists, is(false));
    }

    @Test
    public void shouldReturnMaxIdForFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        Integer maxIdForFarm = entityManager.getEntityManager()
            .createQuery("SELECT MAX(l.id.id) FROM Landparcel l WHERE l.id.farmId = 1", Integer.class)
            .getSingleResult();
        // When
        Optional<Integer> maxId = landparcelRepository.findMaxIdForFarm(farm.getId());

        // Then
        assertThat(maxId, notNullValue());
        assertThat(maxId.get(), is(maxIdForFarm));
    }

    @Test
    public void shouldReturnEmptyOptionalIfNoLandparcelsExistForFarm() {
        //given
        Address address = new Address();
        entityManager.persist(address);
        entityManager.flush();
        ActivationCode activationCode = entityManager.find(ActivationCode.class, 1);
        Farm farm = new Farm();
        farm.setFarmName("uniqueFarmName");
        farm.setIsActive(true);
        farm.setIdAddress(address.getId());
        farm.setIdActivationCode(activationCode.getId());
        entityManager.persist(farm);
        entityManager.flush();
        
        //when
        Optional<Integer> maxId = landparcelRepository.findMaxIdForFarm(farm.getId());
        //then
        assertThat(maxId,is(Optional.empty()));
    }

    @Test
    public void shouldReturnNextFreeIdForFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        Integer maxIdForFarm = entityManager.getEntityManager()
            .createQuery("SELECT MAX(l.id.id) FROM Landparcel l WHERE l.id.farmId = 1", Integer.class)
            .getSingleResult();
        // When
        Integer nextFreeId = landparcelRepository.findNextFreeIdForFarm(farm.getId());

        // Then
        assertThat(nextFreeId, is(maxIdForFarm + 1) ); // Next ID should be 3 since we have IDs 1 and 2
    }

    @Test
    public void shouldReturn1AsNextFreeIdIfNoLandparcelsExist() {
        //given
        Address address = new Address();
        entityManager.persist(address);
        entityManager.flush();
        ActivationCode activationCode = entityManager.find(ActivationCode.class, 1);
        Farm farm = new Farm();
        farm.setFarmName("uniqueFarmName");
        farm.setIsActive(true);
        farm.setIdAddress(address.getId());
        farm.setIdActivationCode(activationCode.getId());
        entityManager.persist(farm);
        entityManager.flush();

        // When
        Integer nextFreeId = landparcelRepository.findNextFreeIdForFarm(farm.getId());

        // Then
        assertThat(nextFreeId, is(1)); 
    }
    
}
