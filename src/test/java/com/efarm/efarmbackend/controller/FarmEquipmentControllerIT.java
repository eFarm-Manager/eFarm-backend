package com.efarm.efarmbackend.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class FarmEquipmentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Test return all equipment from same farm")
    void testReturnAllEquipment() throws Exception {
        //given
        String searchQuery = null;
        User currentUser = entityManager.find(User.class, 1);
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        Farm currentFarm = currentUser.getFarm();
        Long equipmentCount = entityManager.createQuery(
                        "SELECT COUNT(e) FROM FarmEquipment e WHERE e.id.farmId = :farmId", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .getSingleResult();

        //when
        MvcResult result = mockMvc.perform(get("/api/equipment/all")
                        .param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<FarmEquipmentDTO> equipmentDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<FarmEquipmentDTO>>() {
                });

        assertThat(equipmentDTOs.size(), is(equipmentCount.intValue()));
    }

    @Test
    @DisplayName("Test return all equipment from same farm with search query")
    void testReturnEquipmentBySearch() throws Exception {
        //given
        User currentUser = entityManager.find(User.class, 1);
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, 1);
        FarmEquipment firstEquipment = entityManager.find(FarmEquipment.class, farmEquipmentId);
        String searchQuery = firstEquipment.getBrand();
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        Farm currentFarm = currentUser.getFarm();
        Long equipmentCount = entityManager.createQuery(
                        "SELECT COUNT(e) FROM FarmEquipment e WHERE e.id.farmId = :farmId AND e.brand = :brand", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .setParameter("brand", firstEquipment.getBrand())
                .getSingleResult();

        //when
        MvcResult result = mockMvc.perform(get("/api/equipment/all")
                        .param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<FarmEquipmentDTO> equipmentDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<FarmEquipmentDTO>>() {
                });

        assertThat(equipmentDTOs.size(), is(equipmentCount.intValue()));
    }

    @Test
    @DisplayName("Test return equipment from farm with details")
    void testReturnEquipmentDetails() throws Exception {
        //given
        User currentUser = entityManager.find(User.class, 1);
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, 1);
        FarmEquipment firstEquipment = entityManager.find(FarmEquipment.class, farmEquipmentId);
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        //when
        MvcResult result = mockMvc.perform(get("/api/equipment/1"))
                .andExpect(status().isOk())
                .andReturn();

        //then
        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        FarmEquipmentDTO equipmentDTO = objectMapper.readValue(jsonResponse, new TypeReference<FarmEquipmentDTO>() {
        });
        assertThat(equipmentDTO.getEquipmentName(), is(firstEquipment.getEquipmentName()));
        assertThat(equipmentDTO.getEquipmentId(), is(firstEquipment.getId().getId()));
    }


}
