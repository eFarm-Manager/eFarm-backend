package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.model.user.UserSummaryDTO;
import com.efarm.efarmbackend.payload.request.user.ChangeUserPasswordRequest;
import com.efarm.efarmbackend.payload.request.user.UpdateUserRequest;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class UserControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void useOwnerOfFirstFarm() {
        User currentUser = entityManager.find(User.class, 1);
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /*
     * GET /all
     */

    @Test
    @DisplayName("Test return all users from same farm")
    void testReturnUsersByFarmId() throws Exception {
        // Given
        Farm currentFarm = entityManager.find(Farm.class, 1);

        Long userCount = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.farm.id = :farmId", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .getSingleResult();

        // When
        MvcResult result = mockMvc.perform(get("/users/all"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<UserDTO> userDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<UserDTO>>() {
                });

        assertThat(userDTOs.size(), is(userCount.intValue()));
    }

    /*
     * GET /active
     */

    @Test
    @DisplayName("Test return all users from same farm")
    void testReturnActiveUsersByFarmId() throws Exception {
        // Given
        Farm currentFarm = entityManager.find(Farm.class, 1);

        Long userCount = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.farm.id = :farmId AND u.isActive = true", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .getSingleResult();


        // When
        MvcResult result = mockMvc.perform(get("/users/active"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<UserSummaryDTO> userDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<UserSummaryDTO>>() {
                });

        assertThat(userDTOs.size(), is(userCount.intValue()));
    }

    /*
     * PATCH toggle-active/{userId}
     */

    @Test
    public void testToggleUserStatus() throws Exception {
        // Given
        User user = entityManager.createQuery(
                        "select u from User u where u.farm.id = 1 and u.role.name != :ownerRole and u.isActive = true", User.class)
                .setParameter("ownerRole", ERole.ROLE_FARM_OWNER)
                .setMaxResults(1)
                .getSingleResult();

        boolean isActive = user.getIsActive();

        // When
        mockMvc.perform(patch("/users/toggle-active/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Zmieniono status"));

        // Then
        User updatedUser = entityManager.find(User.class, user.getId());
        assertThat(updatedUser.getIsActive(), is(!isActive));
    }

    @Test
    public void testCantToggleUserStatusThatDoesntExist() throws Exception {
        // Given
        Integer userId = 999;

        // When
        mockMvc.perform(patch("/users/toggle-active/{userId}", userId))
                .andExpect(status().isBadRequest())
                //then
                .andExpect(jsonPath("$.message").value("Wybrany użytkownik nie istnieje"));
    }

    @Test
    public void testCantToggleUserStatusFromDifferentFarm() throws Exception {
        // Given
        User user = entityManager.createQuery(
                        "select u from User u where u.farm.id != 1 and u.isActive = true", User.class)
                .setMaxResults(1)
                .getSingleResult();

        // When
        mockMvc.perform(patch("/users/toggle-active/{userId}", user.getId()))
                .andExpect(status().isBadRequest())
                //then
                .andExpect(jsonPath("$.message").value("Wybrany użytkownik nie istnieje"));
    }

    /*
     * PUT /update/{userId}
     */

    @Test
    public void testUpdateUserDetails() throws Exception {
        // Given
        User user = entityManager.createQuery(
                        "select u from User u where u.farm.id = 1 and u.role.name != :ownerRole and u.isActive = true", User.class)
                .setParameter("ownerRole", ERole.ROLE_FARM_OWNER)
                .setMaxResults(1)
                .getSingleResult();

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("NewFirstName");
        updateUserRequest.setLastName("NewLastName");
        updateUserRequest.setEmail("newEmail@gmial.com");
        updateUserRequest.setPhoneNumber("");
        updateUserRequest.setRole("ROLE_FARM_OWNER");

        // When
        mockMvc.perform(put("/users/update/{userId}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUserRequest)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Zaktualizowano dane użytkownika"));

        User updatedUser = entityManager.find(User.class, user.getId());
        assertThat(updatedUser.getFirstName(), is(updateUserRequest.getFirstName()));
        assertThat(updatedUser.getLastName(), is(updateUserRequest.getLastName()));
        assertThat(updatedUser.getEmail(), is(updateUserRequest.getEmail()));
        assertThat(updatedUser.getPhoneNumber(), is(updateUserRequest.getPhoneNumber()));
        assertThat(updatedUser.getRole().getName(), is(ERole.ROLE_FARM_OWNER));
    }

    @Test
    public void testCantUpdateUserDetailsThatDoesntExist() throws Exception {
        // Given
        Integer userId = 999;
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("NewFirstName");
        updateUserRequest.setLastName("NewLastName");
        updateUserRequest.setEmail("newEmail@gmial.com");
        updateUserRequest.setPhoneNumber("");
        updateUserRequest.setRole("ROLE_FARM_OWNER");

        // When
        mockMvc.perform(put("/users/update/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUserRequest)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrany użytkownik nie istnieje"));
    }

    @Test
    public void testCantUpdateUserDetailsFromDifferentFarm() throws Exception {
        // Given
        User user = entityManager.createQuery(
                        "select u from User u where u.farm.id != 1 and u.isActive = true", User.class)
                .setMaxResults(1)
                .getSingleResult();

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("NewFirstName");
        updateUserRequest.setLastName("NewLastName");
        updateUserRequest.setEmail("newEmail@gmial.com");
        updateUserRequest.setPhoneNumber("");
        updateUserRequest.setRole("ROLE_FARM_OWNER");

        // When
        mockMvc.perform(put("/users/update/{userId}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUserRequest)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrany użytkownik nie istnieje"));
    }

    /*
     * PUT /update-password/{userId}
     */

    @Test
    public void testUpdateUserPassword() throws Exception {
        // Given
        User user = entityManager.createQuery(
                        "select u from User u where u.farm.id = 1 and u.role.name != :ownerRole and u.isActive = true", User.class)
                .setParameter("ownerRole", ERole.ROLE_FARM_OWNER)
                .setMaxResults(1)
                .getSingleResult();

        String currentPassword = user.getPassword();
        ChangeUserPasswordRequest request = new ChangeUserPasswordRequest();
        request.setNewPassword("newPassword");

        // When
        mockMvc.perform(put("/users/update-password/{userId}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Zmieniono hasło użytkownika"));

        User updatedUser = entityManager.find(User.class, user.getId());
        assertNotNull(updatedUser);
        assertThat(updatedUser.getPassword(), is(not(currentPassword)));
    }

    @Test
    public void testCantUpdatePasswordForUserThatDoesntExist() throws Exception {
        // Given
        Integer userId = 999;
        ChangeUserPasswordRequest request = new ChangeUserPasswordRequest();
        request.setNewPassword("newPassword");

        // When
        mockMvc.perform(put("/users/update-password/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrany użytkownik nie istnieje"));
    }

    @Test
    public void testCantUpdatePasswordForUserFromDifferentFarm() throws Exception {
        // Given
        User user = entityManager.createQuery(
                        "select u from User u where u.farm.id != 1 and u.isActive = true", User.class)
                .setMaxResults(1)
                .getSingleResult();

        ChangeUserPasswordRequest request = new ChangeUserPasswordRequest();
        request.setNewPassword("newPassword");

        // When
        mockMvc.perform(put("/users/update-password/{userId}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrany użytkownik nie istnieje"));
    }
}
