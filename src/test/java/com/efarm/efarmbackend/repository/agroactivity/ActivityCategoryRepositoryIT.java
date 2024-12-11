package com.efarm.efarmbackend.repository.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.ActivityCategory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class ActivityCategoryRepositoryIT {
    @Autowired
    ActivityCategoryRepository activityCategoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testFindActivityCategoryByName() {
        //given
        ActivityCategory activityCategory = entityManager.find(ActivityCategory.class, 1);

        //when
        Optional<ActivityCategory> foundActivityCategory = activityCategoryRepository.findByName(activityCategory.getName());

        //then
        assertThat(foundActivityCategory.isPresent(), is(true));
        assertThat(foundActivityCategory.get(), notNullValue());
        assertThat(foundActivityCategory.get().getName(), is(activityCategory.getName()));
    }

    @Test
    public void testDoesntFindActivityCategoryByName() {
        //given
        String name = "nonexistent";

        //when
        Optional<ActivityCategory> foundActivityCategory = activityCategoryRepository.findByName(name);

        //then
        assertThat(foundActivityCategory.isEmpty(), is(true));
    }
}
