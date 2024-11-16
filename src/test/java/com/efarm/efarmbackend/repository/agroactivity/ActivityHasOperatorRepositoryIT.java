package com.efarm.efarmbackend.repository.agroactivity;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.efarm.efarmbackend.model.agroactivity.ActivityHasOperator;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId;

import jakarta.transaction.Transactional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class ActivityHasOperatorRepositoryIT {
    @Autowired
    ActivityHasOperatorRepository activityHasOperatorRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testFindActivityHasOperatorByAgroActivity() {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1,1));

        //when
        List<ActivityHasOperator> foundActivityHasOperator = activityHasOperatorRepository.findActivityHasOperatorsByAgroActivity(agroActivity);

        //then
        assertThat(foundActivityHasOperator, not(empty()));
        assertThat(foundActivityHasOperator, everyItem(hasProperty("agroActivity", is(agroActivity))));
    }

    @Test
    public void testDeleteActivityHasOperatorByAgroActivity() {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1,1));

        //when
        activityHasOperatorRepository.deleteActivityHasOperatorsByAgroActivity(agroActivity);

        //then
        List<ActivityHasOperator> foundActivityHasOperator = activityHasOperatorRepository.findActivityHasOperatorsByAgroActivity(agroActivity);
        assertThat(foundActivityHasOperator, is(empty()));
    }
}
