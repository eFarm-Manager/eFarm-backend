package com.efarm.efarmbackend.repository.agriculturalrecords;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;

import jakarta.transaction.Transactional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class AgriculturalRecordRepositoryIT {
    @Autowired
    AgriculturalRecordRepository agriculturalRecordRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testFindByLandparcelAndSeason() {
       //given
        AgriculturalRecord agriculturalRecord = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(1,1));
        Landparcel landparcelTest = agriculturalRecord.getLandparcel();
        Season seasonTest = agriculturalRecord.getSeason();
        Long count = entityManager.getEntityManager()
                .createQuery("SELECT COUNT(ar) FROM AgriculturalRecord ar WHERE ar.season = :season AND ar.landparcel=:landparcel", Long.class)
                .setParameter("season", seasonTest)
                .setParameter("landparcel", landparcelTest)
                .getSingleResult();

        //when
        List<AgriculturalRecord> foundRecords = agriculturalRecordRepository.findByLandparcelAndSeason(landparcelTest, seasonTest);

        //then
        assertThat(foundRecords.size(), is(count.intValue()));
        assertThat(foundRecords, notNullValue());
        assertThat(foundRecords.contains(agriculturalRecord), is(true));
        assertThat(foundRecords, everyItem(hasProperty("id", hasProperty("farmId", is(1)))));
    }

    @Test
    public void testFindByLandparcelAndSeasonAndCrop() {
        //given
        AgriculturalRecord agriculturalRecord = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(1,1));
        Landparcel landparcelTest = agriculturalRecord.getLandparcel();
        Season seasonTest = agriculturalRecord.getSeason();
        Crop cropTest = agriculturalRecord.getCrop();
        Long count = entityManager.getEntityManager()
                .createQuery("SELECT COUNT(ar) FROM AgriculturalRecord ar WHERE ar.season = :season AND ar.landparcel=:landparcel AND ar.crop=:crop", Long.class)
                .setParameter("season", seasonTest)
                .setParameter("landparcel", landparcelTest)
                .setParameter("crop", cropTest)
                .getSingleResult();

        //when
        List<AgriculturalRecord> foundRecords = agriculturalRecordRepository.findByLandparcelAndSeasonAndCrop(landparcelTest, seasonTest, cropTest);

        //then
        assertThat(foundRecords.size(), is(count.intValue()));
        assertThat(foundRecords, notNullValue());
        assertThat(foundRecords.contains(agriculturalRecord), is(true));
        assertThat(foundRecords, everyItem(hasProperty("id", hasProperty("farmId", is(1)))));
    }

    @Test
    public void testFindMaxIdForFarm() {
        //given
        Integer farmId = 1;
        Integer maxIdForFarm = entityManager.getEntityManager()
                .createQuery("SELECT MAX(ar.id.id) FROM AgriculturalRecord ar where ar.id.farmId = :farmId", Integer.class)
                .setParameter("farmId", farmId)
                .getSingleResult();

        //when
        Optional<Integer> maxId = agriculturalRecordRepository.findMaxIdForFarm(farmId);

        //then
        assertThat(maxId, notNullValue());
        assertThat(maxId.get(), is(maxIdForFarm));
    }

    @Test
    public void shouldReturnEmptyOptionalIfNoAgriculturalRecordsExistForFarm() {
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
        Optional<Integer> maxId = agriculturalRecordRepository.findMaxIdForFarm(farm.getId());
        //then
        assertThat(maxId,is(Optional.empty()));
    }

    @Test
    public void shouldReturnNextFreeIdForFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        Integer maxIdForFarm = entityManager.getEntityManager()
            .createQuery("SELECT MAX(ar.id.id) FROM AgriculturalRecord ar WHERE ar.id.farmId = 1", Integer.class)
            .getSingleResult();
        // When
        Integer nextFreeId = agriculturalRecordRepository.findNextFreeIdForFarm(farm.getId());

        // Then
        assertThat(nextFreeId, is(maxIdForFarm + 1) ); 
    }

    @Test
    public void shouldReturn1AsNextFreeIdIfNoAgriculturalRecordExist() {
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
        Integer nextFreeId = agriculturalRecordRepository.findNextFreeIdForFarm(farm.getId());

        // Then
        assertThat(nextFreeId, is(1)); 
    }
}
