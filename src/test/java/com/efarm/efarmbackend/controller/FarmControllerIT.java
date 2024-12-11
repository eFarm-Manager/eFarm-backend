package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farm.FarmDTO;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
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

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

    @BeforeEach
    public void useOwnerOfFirstFarm() {
        User currentUser = entityManager.find(User.class, 1);
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /*
     * GET /details
     */

    @Test
    @DisplayName("Test return farm, address and code expiration date details")
    void testDetailsOfFarm() throws Exception {
        //given
        Farm userFarm = entityManager.find(Farm.class, 1);
        Address farmAddress = entityManager.find(Address.class, userFarm.getIdAddress());

        //when
        MvcResult result = mockMvc.perform(get("/farm/details"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        FarmDTO farmDTO = objectMapper.readValue(jsonResponse, new TypeReference<FarmDTO>() {
        });

        assertNotNull(farmDTO);
        assertEquals(userFarm.getFarmName(), farmDTO.getFarmName());
        assertEquals(userFarm.getFarmNumber(), farmDTO.getFarmNumber());
        assertEquals(farmAddress.getStreet(), farmDTO.getStreet());
        assertEquals(farmAddress.getCity(), farmDTO.getCity());
    }
    /*
     * PUT /details
     */

    @Test
    @DisplayName("Test successfuly update farm and address details")
    void testUpdateFarmDetails() throws Exception {
        //given
        UpdateFarmDetailsRequest updateFarmDetailsRequest = new UpdateFarmDetailsRequest();
        updateFarmDetailsRequest.setFarmName("New Farm");
        updateFarmDetailsRequest.setFarmNumber("202");
        updateFarmDetailsRequest.setSanitaryRegisterNumber("101");
        updateFarmDetailsRequest.setStreet("ulica Y");
        updateFarmDetailsRequest.setBuildingNumber("20D");
        updateFarmDetailsRequest.setCity("Miasto X");

        Farm userFarm = entityManager.find(Farm.class, 1);
        Address farmAddress = entityManager.find(Address.class, userFarm.getIdAddress());
        updateFarmDetailsRequest.setFeedNumber(userFarm.getFeedNumber());
        updateFarmDetailsRequest.setZipCode(farmAddress.getZipCode());

        //when
        mockMvc.perform(put("/farm/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateFarmDetailsRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then: 
        Farm updatedFarm = entityManager.find(Farm.class, userFarm.getId());
        Address updatedAddress = entityManager.find(Address.class, updatedFarm.getIdAddress());

        assertNotNull(updatedFarm);
        assertNotNull(updatedAddress);

        assertThat("New Farm", is(updatedFarm.getFarmName()));
        assertThat("202", is(updatedFarm.getFarmNumber()));
        assertThat(userFarm.getFeedNumber(), is(updatedFarm.getFeedNumber()));
        assertThat("101", is(updatedFarm.getSanitaryRegisterNumber()));
        assertThat("ulica Y", is(updatedAddress.getStreet()));
        assertThat("20D", is(updatedAddress.getBuildingNumber()));
        assertThat(farmAddress.getZipCode(), is(updatedAddress.getZipCode()));
        assertThat("Miasto X", is(updatedAddress.getCity()));
    }

    @Test
    @DisplayName("Test successfully update farm and address details with a new user")
    void testUpdateFarmDetailsWithNewUser() throws Exception {
        // Given
        ActivationCode activationCode = entityManager.createQuery(
                        "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
                .setParameter("used", false)
                .setMaxResults(1)
                .getSingleResult();

        User newUser = new User();
        newUser.setFirstName("testFirstName");
        newUser.setLastName("testLastName");
        newUser.setEmail("testEmail@gmail.com");
        newUser.setUsername("testUser");
        newUser.setPassword("password123");
        Role owner = entityManager.find(Role.class, 3);
        newUser.setRole(owner);
        newUser.setIsActive(true);

        Farm newFarm = new Farm();
        newFarm.setFarmName("Test Farm");
        newFarm.setIdActivationCode(activationCode.getId());
        newFarm.setIsActive(true);

        Address newAddress = new Address();
        entityManager.persist(newAddress);

        newFarm.setIdAddress(newAddress.getId());

        entityManager.persist(newFarm);

        newUser.setFarm(newFarm);
        entityManager.persist(newUser);

        UpdateFarmDetailsRequest updateFarmDetailsRequest = new UpdateFarmDetailsRequest();
        updateFarmDetailsRequest.setFarmName("New Farm");
        updateFarmDetailsRequest.setFarmNumber("202");
        updateFarmDetailsRequest.setSanitaryRegisterNumber("101");
        updateFarmDetailsRequest.setStreet("ulica Y");
        updateFarmDetailsRequest.setBuildingNumber("20D");
        updateFarmDetailsRequest.setCity("Miasto X");
        updateFarmDetailsRequest.setFeedNumber("");
        updateFarmDetailsRequest.setZipCode("");

        UserDetailsImpl userDetails = UserDetailsImpl.build(newUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // When
        mockMvc.perform(put("/farm/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateFarmDetailsRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then
        Farm updatedFarm = entityManager.find(Farm.class, newFarm.getId());
        Address updatedAddress = entityManager.find(Address.class, updatedFarm.getIdAddress());

        assertNotNull(updatedFarm);
        assertNotNull(updatedAddress);

        assertThat("New Farm", is(updatedFarm.getFarmName()));
        assertThat("202", is(updatedFarm.getFarmNumber()));
        assertThat("", is(updatedFarm.getFeedNumber()));
        assertThat("101", is(updatedFarm.getSanitaryRegisterNumber()));
        assertThat("ulica Y", is(updatedAddress.getStreet()));
        assertThat("20D", is(updatedAddress.getBuildingNumber()));
        assertThat("", is(updatedAddress.getZipCode()));
        assertThat("Miasto X", is(updatedAddress.getCity()));
    }

    @Test
    public void testThatFarmDetailsCantBeUpdatedWhenFarmNameAlreadyExists() throws Exception {
        // Given
        User currentUser = entityManager.find(User.class, 1);
        Farm userFarm = currentUser.getFarm();

        Farm otherFarm = entityManager.createQuery(
                        "SELECT f FROM Farm f WHERE f.id = :farmId", Farm.class)
                .setParameter("farmId", userFarm.getId() + 1)
                .setMaxResults(1)
                .getSingleResult();

        UpdateFarmDetailsRequest updateFarmDetailsRequest = new UpdateFarmDetailsRequest();
        updateFarmDetailsRequest.setFarmName(otherFarm.getFarmName());
        updateFarmDetailsRequest.setFarmNumber("202");
        updateFarmDetailsRequest.setSanitaryRegisterNumber("101");
        updateFarmDetailsRequest.setStreet("ulica Y");
        updateFarmDetailsRequest.setBuildingNumber("20D");
        updateFarmDetailsRequest.setCity("Miasto X");
        updateFarmDetailsRequest.setFeedNumber(userFarm.getFeedNumber());
        updateFarmDetailsRequest.setZipCode("");

        // When
        mockMvc.perform(put("/farm/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateFarmDetailsRequest)))
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrana nazwa farmy jest zajęta. Spróbuj wybrać inną."));
    }

}