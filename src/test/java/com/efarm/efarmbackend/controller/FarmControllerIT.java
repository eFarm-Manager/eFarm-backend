package com.efarm.efarmbackend.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.efarm.efarmbackend.service.FarmService;
import com.efarm.efarmbackend.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional	
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class FarmControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FarmService farmService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Test return all users from same farm")
    void testReturnUsersByFarmId() throws Exception {
        // Given
        User currentUser = entityManager.createQuery(
            "SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName", User.class)
            .setParameter("roleName", ERole.ROLE_FARM_OWNER)
            .setMaxResults(1)  // Ensures only one result is returned
            .getSingleResult();
        Farm currentFarm = currentUser.getFarm();

        Long userCount = entityManager.createQuery(
            "SELECT COUNT(u) FROM User u WHERE u.farm.id = :farmId", Long.class)
            .setParameter("farmId", currentFarm.getId())
            .getSingleResult();

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);


        // When
        MvcResult result = mockMvc.perform(get("/api/farm/users"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<UserDTO> userDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(), 
                new TypeReference<List<UserDTO>>() {});
                
        assertThat(userDTOs.size(), is(userCount.intValue()));
    }

    @Test
    @DisplayName("Test access is denied for unauthorized users")
    void testUnauthorizedAccess() throws Exception {
        // Given
        User currentUser = entityManager.createQuery(
            "SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName", User.class)
            .setParameter("roleName", ERole.ROLE_FARM_EQUIPMENT_OPERATOR)
            .setMaxResults(1)  // Ensures only one result is returned
            .getSingleResult();
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // When
        mockMvc.perform(get("/api/farm/users"))
                .andExpect(status().isForbidden()); 
    }
}