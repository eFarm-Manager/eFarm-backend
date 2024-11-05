package com.efarm.efarmbackend.repository.landparcel;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
        assertThat(result, notNullValue());
        assertThat(result, everyItem(hasProperty("id", hasProperty("farmId", is(1)))));
    }

    @Test
    public void shouldReturnEmptyListForUnknownFarmId() {
        // When
        List<Landparcel> result = landparcelRepository.findByFarmId(999); 

        // Then
        assertThat(result.size(),is(0));
        assertThat(result, is(empty()));
    }

    @Test
    public void shouldReturnTrueIfLandparcelExistsWithGivenParameters() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        Landparcel existingLandparcel = entityManager.getEntityManager()
            .createQuery("SELECT l FROM Landparcel l WHERE l.id.farmId = 1 AND l.id.id = 1", Landparcel.class)
            .getSingleResult();

        String geodesyLandparcelNumber = existingLandparcel.getGeodesyLandparcelNumber();

        // When
        Boolean exists = landparcelRepository.existsByGeodesyLandparcelNumberAndFarm(geodesyLandparcelNumber, farm);

        // Then
        assertThat(exists,is(true));
    }

    @Test
    public void shouldReturnFalseIfLandparcelDoesNotExistWithGivenParameters() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        
        // When
        Boolean exists = landparcelRepository.existsByGeodesyLandparcelNumberAndFarm("geodesyLandparcelNumberUnknown", farm);

        // Then
        assertThat(exists, is(false));
    }

    @Test
    public void testExistsByFarmAndName() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        Landparcel existingLandparcel = entityManager.getEntityManager()
            .createQuery("SELECT l FROM Landparcel l WHERE l.id.farmId = 1 AND l.id.id = 1", Landparcel.class)
            .getSingleResult();

        String name = existingLandparcel.getName();

        // When
        Boolean exists = landparcelRepository.existsByFarmAndName(farm, name);

        // Then
        assertThat(exists, is(true));
    }

    @Test
    public void testDoesNotExistsByFarmAndName() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        
        // When
        Boolean exists = landparcelRepository.existsByFarmAndName(farm, "nameUnknown");

        // Then
        assertThat(exists, is(false));
    }

    @Test
    public void testfindByFarmIdAndIsAvailableTrue() {
        //given
        Integer farmId = 1;
        Long countActive = entityManager.getEntityManager()
                .createQuery("SELECT COUNT(l) FROM Landparcel l WHERE l.id.farmId = 1 AND l.isAvailable = true", Long.class)
                .getSingleResult();
        
        // When
        List<Landparcel> result = landparcelRepository.findByFarmIdAndIsAvailableTrue(farmId);

        // Then
        assertThat(result.size(), is(countActive.intValue()));
        assertThat(result, notNullValue());
        assertThat(result, everyItem(hasProperty("id", hasProperty("farmId", is(1)))));
    }

    @Test
    public void testfindByFarmIdAndIsAvailableTrueEmptyForUnknownFarmId() {
        // When
        List<Landparcel> result = landparcelRepository.findByFarmIdAndIsAvailableTrue(999); 

        // Then
        assertThat(result.size(),is(0));
        assertThat(result, is(empty()));
    }

    @Test
    public void shouldGetSumOfAvailableArea() {
        //given
        Integer farmId = 1;
        Double sumArea = entityManager.getEntityManager()
            .createQuery("SELECT SUM(l.area) FROM Landparcel l WHERE " + 
            "l.farm.id = :farmId AND l.isAvailable = true AND " + 
            "(l.landOwnershipStatus.ownershipStatus = :privatelyOwnedStatus OR " +
            "l.landOwnershipStatus.ownershipStatus = :leaseStatus)", 
        Double.class)
        .setParameter("farmId", farmId)
        .setParameter("privatelyOwnedStatus", ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
        .setParameter("leaseStatus", ELandOwnershipStatus.STATUS_LEASE)
        .getSingleResult();

        // When
        Double result = landparcelRepository.sumAvailableLandArea(farmId);

        // Then
        assertThat(result, is(sumArea));
    }

    @Test
    public void shouldReturnSumAvailableLandAreaByStatus() {
        //given
        Integer farmId = 1;
        ELandOwnershipStatus status = ELandOwnershipStatus.STATUS_PRIVATELY_OWNED;
        Double sumArea = entityManager.getEntityManager()
            .createQuery("SELECT SUM(l.area) FROM Landparcel l WHERE " + 
            "l.farm.id = :farmId AND l.isAvailable = true AND " + 
            "l.landOwnershipStatus.ownershipStatus = :status", 
        Double.class)
        .setParameter("farmId", farmId)
        .setParameter("status", status)
        .getSingleResult();

        // When
        Double result = landparcelRepository.sumAvailableLandAreaByStatus(farmId, status);

        // Then
        assertThat(result, is(sumArea));
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
        assertThat(nextFreeId, is(maxIdForFarm + 1) );
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
