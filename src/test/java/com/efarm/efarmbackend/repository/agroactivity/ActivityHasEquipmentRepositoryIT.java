package com.efarm.efarmbackend.repository.agroactivity;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.efarm.efarmbackend.model.agroactivity.ActivityHasEquipment;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId;

import jakarta.transaction.Transactional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class ActivityHasEquipmentRepositoryIT {

    @Autowired
    ActivityHasEquipmentRepository activityHasEquipmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testFindActivityHasEquipmentByAgroActivity() {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1,1));

        //when
        List<ActivityHasEquipment> foundActivityHasEquipment = activityHasEquipmentRepository.findActivityHasEquipmentsByAgroActivity(agroActivity);

        //then
        assertThat(foundActivityHasEquipment, not(empty()));
        assertThat(foundActivityHasEquipment, everyItem(hasProperty("agroActivity", is(agroActivity))));
    }

    @Test
    public void testDeleteActivityHasEquipmentByAgroActivity() {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1,1));

        //when
        activityHasEquipmentRepository.deleteActivityHasEquipmentsByAgroActivity(agroActivity);

        //then
        List<ActivityHasEquipment> foundActivityHasEquipment = activityHasEquipmentRepository.findActivityHasEquipmentsByAgroActivity(agroActivity);
        assertThat(foundActivityHasEquipment, empty());
    }
    
}
