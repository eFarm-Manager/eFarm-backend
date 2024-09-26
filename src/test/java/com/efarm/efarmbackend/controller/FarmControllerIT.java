package com.efarm.efarmbackend.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.efarm.efarmbackend.repository.user.RoleRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farm.FarmDTO;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.payload.request.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.security.jwt.JwtUtils;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import groovyjarjarantlr4.v4.parse.GrammarTreeVisitor.ruleModifier_return;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class FarmControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

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

    @Test
    @DisplayName("Test return farm, address and code expiration date details")
    void testDetailsOfFarm() throws Exception {
        //given
        User currentUser = entityManager.createQuery(
            "SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName", User.class)
            .setParameter("roleName", ERole.ROLE_FARM_MANAGER)
            .setMaxResults(1)  
            .getSingleResult();
        Farm userFarm = currentUser.getFarm();
        Address farmAddress = entityManager.find(Address.class, userFarm.getIdAddress()); 

        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        //when
        MvcResult result = mockMvc.perform(get("/api/farm/details"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        FarmDTO farmDTO = new ObjectMapper().readValue(jsonResponse, new TypeReference<FarmDTO>() {});

        assertNotNull(farmDTO);
        assertEquals(userFarm.getFarmName(), farmDTO.getFarmName());
        assertEquals(userFarm.getFarmNumber(), farmDTO.getFarmNumber());
        assertEquals(farmAddress.getStreet(), farmDTO.getStreet());
        assertEquals(farmAddress.getCity(), farmDTO.getCity());
    }

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
        

        User currentUser = entityManager.createQuery(
            "SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName", User.class)
            .setParameter("roleName", ERole.ROLE_FARM_OWNER)
            .setMaxResults(1)  
            .getSingleResult();
        Farm userFarm = currentUser.getFarm();
        Address farmAddress = entityManager.find(Address.class, userFarm.getIdAddress()); 
        updateFarmDetailsRequest.setFeedNumber(userFarm.getFeedNumber());
        updateFarmDetailsRequest.setZipCode(farmAddress.getZipCode());
        
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication());
        System.out.println("Authenticated: " + SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        System.out.println("Authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        //when
        mockMvc.perform(put("/api/farm/details") 
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
        // Given: Create a new user
        ActivationCode activationCode = entityManager.createQuery(
                    "SELECT a FROM ActivationCode a WHERE a.isUsed = :used", ActivationCode.class)
            .setParameter("used", false)
            .setMaxResults(1)  // Ensures only one result is returned
            .getSingleResult();

        User newUser = new User();
        newUser.setFirstName("testFirstName");
        newUser.setLastName("testLastName");
        newUser.setEmail("testEmail@gmail.com");
        newUser.setUsername("testUser");
        newUser.setPassword("password123"); 
        Optional<Role> owner = roleRepository.findByName(ERole.ROLE_FARM_OWNER);
        newUser.setRole(owner.get()); 
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

        // Prepare the update request
        UpdateFarmDetailsRequest updateFarmDetailsRequest = new UpdateFarmDetailsRequest();
        updateFarmDetailsRequest.setFarmName("New Farm");
        updateFarmDetailsRequest.setFarmNumber("202");
        updateFarmDetailsRequest.setSanitaryRegisterNumber("101");
        updateFarmDetailsRequest.setStreet("ulica Y");
        updateFarmDetailsRequest.setBuildingNumber("20D");
        updateFarmDetailsRequest.setCity("Miasto X");
        updateFarmDetailsRequest.setFeedNumber("");
        updateFarmDetailsRequest.setZipCode("");

        // Authenticate the new user
        UserDetailsImpl userDetails = UserDetailsImpl.build(newUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // When: Perform the update operation
        mockMvc.perform(put("/api/farm/details") 
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(updateFarmDetailsRequest)))
            .andDo(print())
            .andExpect(status().isOk()) 
            .andReturn();

        // Then: Verify the update
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

}