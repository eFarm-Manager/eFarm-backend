package com.efarm.efarmbackend.repository.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
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
import static org.hamcrest.Matchers.*;

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
        AgriculturalRecord agriculturalRecord = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(1, 1));
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
        AgriculturalRecord agriculturalRecord = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(1, 1));
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
    public void testFindAgriculturalRecordByFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        Long count = entityManager.getEntityManager()
                .createQuery("SELECT COUNT(ar) FROM AgriculturalRecord ar WHERE ar.id.farmId = :farmId", Long.class)
                .setParameter("farmId", farm.getId())
                .getSingleResult();

        //when
        List<AgriculturalRecord> foundRecords = agriculturalRecordRepository.findAgriculturalRecordByFarm(farm);

        //then
        assertThat(foundRecords.size(), is(count.intValue()));
        assertThat(foundRecords, notNullValue());
        assertThat(foundRecords, everyItem(hasProperty("id", hasProperty("farmId", is(farm.getId())))));
    }

    @Test
    public void testFindCropStatisticsBySeasonAndFarm() {
        //given
        Integer seasonId = 1;
        Integer farmId = 1;
        List<Object[]> cropStatistics = entityManager.getEntityManager()
                .createQuery("SELECT ar.crop.name, SUM(ar.area) " +
                        "FROM AgriculturalRecord ar " +
                        "WHERE ar.season.id = :seasonId AND ar.landparcel.farm.id = :farmId " +
                        "GROUP BY ar.crop.name", Object[].class)
                .setParameter("seasonId", seasonId)
                .setParameter("farmId", farmId)
                .getResultList();

        //when
        List<Object[]> foundCropStatistics = agriculturalRecordRepository.findCropStatisticsBySeasonAndFarm(seasonId, farmId);

        //then
        assertThat(foundCropStatistics, notNullValue());
        assertThat(foundCropStatistics.size(), is(cropStatistics.size()));
        for (int i = 0; i < foundCropStatistics.size(); i++) {
            assertThat(foundCropStatistics.get(i)[0], is(cropStatistics.get(i)[0]));
            assertThat(foundCropStatistics.get(i)[1], is(cropStatistics.get(i)[1]));
        }
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
        assertThat(maxId, is(Optional.empty()));
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
        assertThat(nextFreeId, is(maxIdForFarm + 1));
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
