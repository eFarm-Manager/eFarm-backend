package com.efarm.efarmbackend.repository.user;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;


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
public class UserRepositoryIT {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Tests loading existing user in database by login")
    public void testFindByUsername() {
        // Given
        User user = entityManager.find(User.class, 1);
        String usernameTest = user.getUsername();

        // When
        Optional<User> foundUser = userRepository.findByUsername(usernameTest);

        // Then
        assertThat(foundUser.isPresent(), is(true));
        assertThat(foundUser.get(), notNullValue());
        assertThat(foundUser.get().getUsername(), is(usernameTest));
    }

    @Test
    @DisplayName("Tests adding new user to database and finding by username")
    public void testFindNewUserByUsername() {
        // Given
        String usernameTest = "user1";

        Role role = entityManager.find(Role.class, 2);
        Farm farm = entityManager.find(Farm.class, 5);

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password");
        testUser.setRole(role);
        testUser.setFarm(farm);
        testUser.setIsActive(true);

        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Optional<User> foundUser = userRepository.findByUsername(usernameTest);

        // Then
        assertThat(foundUser.isPresent(), is(true));
        assertThat(foundUser.get(), notNullValue());
        assertThat(foundUser.get().getUsername(), is(usernameTest));
        assertThat(foundUser.get().getFarm(), is(farm));
        assertThat(foundUser.get().getRole(), is(role));
    }


    @Test
    @DisplayName("Tests comparison of 2 different users")
    public void testDifferentUserNotSame() {
        // Given
        User user1 = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(2L).orElseThrow(() -> new RuntimeException("User not found"));

        String usernameTest = user1.getUsername();

        // When
        Optional<User> foundUser = userRepository.findByUsername(usernameTest);

        // Then
        assertThat(foundUser.get().getUsername(), not(user2.getUsername()));
    }

    @Test
    @DisplayName("Tests that no user is found by username if username does not exists")
    public void testUserNotExist() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("user1");

        // Then
        assertThat(foundUser.isPresent(), is(false));
    }

    @Test
    @DisplayName("Tests that user existing in db exists by username")
    public void testUserExistsByUsername() {
        //given
        User user = entityManager.find(User.class, 1);

        String usernameTest = user.getUsername();

        //when
        Boolean existsByUsername = userRepository.existsByUsername(usernameTest);

        //then
        assertThat(existsByUsername, is(true));
    }

    @Test
    @DisplayName("Tests adding new user to database and finding if it exists by username")
    public void testNewUserExistsByUsername() {
        // Given
        String usernameTest = "user1";

        Role role = entityManager.find(Role.class, 2);
        Farm farm = entityManager.find(Farm.class, 5);

        User testUser = new User();
        testUser.setUsername(usernameTest);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password");
        testUser.setRole(role);
        testUser.setFarm(farm);
        testUser.setIsActive(true);

        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Boolean existsByUsername = userRepository.existsByUsername(usernameTest);

        // Then
        assertThat(existsByUsername, is(true));
    }


    @Test
    @DisplayName("Tests that user does not exist for not existing username")
    public void testUserDoesNotExistsByUsername() {
        //given
        String usernameTest = "user1";

        //when
        Boolean existsByUsername = userRepository.existsByUsername(usernameTest);

        //then
        assertThat(existsByUsername, is(false));
    }

    @Test
    @DisplayName("Tests that all users are found by farm id")
    public void testFindAllUsersByFarmId() {
        // Given
        Integer farmId = 1;

        // When
        List<User> users = userRepository.findByFarmId(farmId);

        // Then
        assertThat(users, is(not(empty())));
        assertThat(users, everyItem(hasProperty("farm", hasProperty("id", is(farmId)))));
    }

}
