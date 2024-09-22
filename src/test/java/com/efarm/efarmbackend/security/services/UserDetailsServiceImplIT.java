package com.efarm.efarmbackend.security.services;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class UserDetailsServiceImplIT {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("Test that takes user from db and loads its details by username")
    void testLoadUserByUsernameCorrectly() throws Exception {
        // Given
        User user1 = entityManager.find(User.class, 1);
        String usernameTest = user1.getUsername();

        // When
        UserDetails foundUser = userDetailsService.loadUserByUsername(usernameTest);

        // Then
        assertThat(foundUser, notNullValue());
        assertThat(foundUser.getUsername(), is(usernameTest));
    }

    @Test
    @DisplayName("Test that creates new user and finds his details by username")
    void testLoadCreatedUserByUsernameCorrectly() throws Exception {
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
        UserDetails foundUser = userDetailsService.loadUserByUsername(usernameTest);

        // Then
        assertThat(foundUser, notNullValue());
        assertThat(foundUser.getUsername(), is(usernameTest));
    }

    @Test
    @DisplayName("Test that doesnt find user details since username does not exist")
    void testNotFindNonExistingUser() throws Exception {
        // Given
        String usernameTest = "user1";

        //Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(usernameTest);
        });
    }
}

