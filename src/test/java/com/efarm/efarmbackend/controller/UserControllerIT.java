package com.efarm.efarmbackend.controller;

import org.junit.jupiter.api.*;
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

import com.efarm.efarmbackend.repository.user.RoleRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farm.FarmDTO;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.model.user.UserSummaryDTO;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;


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
}
