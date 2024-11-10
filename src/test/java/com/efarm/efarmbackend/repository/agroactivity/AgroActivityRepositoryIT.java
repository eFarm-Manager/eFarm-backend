package com.efarm.efarmbackend.repository.agroactivity;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agroactivity.ActivityCategory;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId;
import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;

import jakarta.transaction.Transactional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class AgroActivityRepositoryIT {
    @Autowired
    AgroActivityRepository agroActivityRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testFindByAgriculturalRecordById() {
        //given
        AgriculturalRecord agriculturalRecord = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(1,1));

        //when
        List<AgroActivity> foundAgroActivity = agroActivityRepository.findByAgriculturalRecordId(agriculturalRecord.getId());

        //then
        assertThat(foundAgroActivity, not(empty()));
        assertThat(foundAgroActivity, everyItem(hasProperty("agriculturalRecord", is(agriculturalRecord))));
    }

    @Test
    public void testDoesntFindByAgriculturalRecordById() {
        //given
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(999,1);

        //when
        List<AgroActivity> foundAgroActivity = agroActivityRepository.findByAgriculturalRecordId(agriculturalRecordId);

        //then
        assertThat(foundAgroActivity, is(empty()));
    }

    @Test
    public void testFindByAgriculturalRecord() {
        //given
        AgriculturalRecord agriculturalRecord = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(1,1));

        //when
        List<AgroActivity> foundAgroActivity = agroActivityRepository.findByAgriculturalRecord(agriculturalRecord);

        //then
        assertThat(foundAgroActivity, not(empty()));
        assertThat(foundAgroActivity, everyItem(hasProperty("agriculturalRecord", is(agriculturalRecord))));
    }

    @Test
    public void testDoesntFindByAgriculturalRecord() {
        //given
        AgriculturalRecord agriculturalRecord = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(999,1));

        //when
        List<AgroActivity> foundAgroActivity = agroActivityRepository.findByAgriculturalRecord(agriculturalRecord);

        //then
        assertThat(foundAgroActivity, is(empty()));
    }

    @Test
    public void testFindWithDetailsById() {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1,1));
        AgriculturalRecord agriculturalRecord = agroActivity.getAgriculturalRecord();
        ActivityCategory activityCategory = agroActivity.getActivityCategory();

        //when
        Optional<AgroActivity> foundAgroActivity = agroActivityRepository.findWithDetailsById(agroActivity.getId());

        //then
        assertThat(foundAgroActivity.isPresent(), is(true));
        assertThat(foundAgroActivity.get(), notNullValue());
        assertThat(foundAgroActivity.get().getId(), is(agroActivity.getId()));
        assertThat(foundAgroActivity.get().getAgriculturalRecord(), is(agriculturalRecord));
        assertThat(foundAgroActivity.get().getActivityCategory(), is(activityCategory));
    }

    @Test
    public void testFindMaxAgroActivityIdForFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);

        Integer maxId = entityManager.getEntityManager()
        .createQuery("SELECT MAX(aa.id.id) FROM AgroActivity aa WHERE aa.id.farmId = 1", Integer.class)
        .getSingleResult();

        //when
        Optional<Integer> maxIdFound = agroActivityRepository.findMaxIdForFarm(farm.getId());

        //then
        System.out.println(maxId);
        assertThat(maxIdFound.get(),is(maxId));
    }

    @Test
    public void testFindMaxAgroActivityIdForNewFarm() {
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
        Optional<Integer> maxIdFound = agroActivityRepository.findMaxIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound,is(Optional.empty()));
    }

    @Test
    public void testFindNextMaxFreeAgroActivityIdForFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);

        Integer maxId = entityManager.getEntityManager()
        .createQuery("SELECT MAX(aa.id.id) FROM AgroActivity aa WHERE aa.id.farmId = 1", Integer.class)
        .getSingleResult();

        //when
        Integer maxIdFound = agroActivityRepository.findNextFreeIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound,is(maxId+1));
    }

    @Test
    public void testFindNextMaxFreeAgroActivityIdForNewFarm() {
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
        Integer maxIdFound = agroActivityRepository.findNextFreeIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound,is(1));
    }
}
