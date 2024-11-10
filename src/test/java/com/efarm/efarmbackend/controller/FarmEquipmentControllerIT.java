package com.efarm.efarmbackend.controller;


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

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    public void useOwnerOfFirstFarm() {
        User currentUser = entityManager.find(User.class, 1);
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
    /*
      *  GET /all
     */

    @Test
    @DisplayName("Test return all equipment from same farm")
    void testReturnAllEquipment() throws Exception {
        //given
        String searchQuery = null;
        Farm currentFarm = entityManager.find(Farm.class, 1);
        Long equipmentCount = entityManager.createQuery(
                        "SELECT COUNT(e) FROM FarmEquipment e WHERE e.id.farmId = :farmId", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .getSingleResult();

        //when
        MvcResult result = mockMvc.perform(get("/equipment/all")
                        .param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<EquipmentSummaryDTO> equipmentDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<EquipmentSummaryDTO>>() {
                });

        assertThat(equipmentDTOs.size(), is(equipmentCount.intValue()));
    }

    @Test
    @DisplayName("Test return all equipment from same farm with search query")
    void testReturnEquipmentBySearch() throws Exception {
        //given
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, 1);
        FarmEquipment firstEquipment = entityManager.find(FarmEquipment.class, farmEquipmentId);
        String searchQuery = firstEquipment.getBrand();

        Farm currentFarm = entityManager.find(Farm.class, 1);
        Long equipmentCount = entityManager.createQuery(
                        "SELECT COUNT(e) FROM FarmEquipment e WHERE e.id.farmId = :farmId AND e.brand = :brand", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .setParameter("brand", firstEquipment.getBrand())
                .getSingleResult();

        //when
        MvcResult result = mockMvc.perform(get("/equipment/all")
                        .param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<EquipmentSummaryDTO> equipmentDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<EquipmentSummaryDTO>>() {
                });

        assertThat(equipmentDTOs.size(), is(equipmentCount.intValue()));
    }
    /*
     *  GET /{equipmentId}
    */

    @Test
    @DisplayName("Test return equipment from farm with details")
    void testReturnEquipmentDetails() throws Exception {
        //given
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1, 1);
        FarmEquipment firstEquipment = entityManager.find(FarmEquipment.class, farmEquipmentId);

        //when
        MvcResult result = mockMvc.perform(get("/equipment/1"))
                .andExpect(status().isOk())
                .andReturn();

        //then
        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        AddUpdateFarmEquipmentRequest equipmentDTO = objectMapper.readValue(jsonResponse, new TypeReference<AddUpdateFarmEquipmentRequest>() {
        });
        assertThat(equipmentDTO.getEquipmentName(), is(firstEquipment.getEquipmentName()));
        assertThat(equipmentDTO.getEquipmentId(), is(firstEquipment.getId().getId()));
    }

    /*
     *  PUT /{equipmentId}
     */
    @Test
    @DisplayName("Test updating farm equipment successfully")
    public void testUpdateFarmEquipmentSuccess() throws Exception {
        // Given
        Integer equipmentId = 1;
        AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest = new AddUpdateFarmEquipmentRequest();
        addUpdateFarmEquipmentRequest.setEquipmentName("Updated Name");

        //when
        mockMvc.perform(put("/equipment/{equipmentId}", equipmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addUpdateFarmEquipmentRequest)))
                .andDo(print())
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie zaktualizowane dane maszyny."));
    }

    @Test
    @DisplayName("Test updating farm equipment that does not exist")
    public void testUpdateFarmEquipmentNotFound() throws Exception {
        // Given
        Integer equipmentId = 999; 
        AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest = new AddUpdateFarmEquipmentRequest();
        addUpdateFarmEquipmentRequest.setEquipmentName("Updated Name");
    
        // When
        mockMvc.perform(put("/equipment/{equipmentId}", equipmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addUpdateFarmEquipmentRequest)))
                .andDo(print())
        //then
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.message").value("Nie znaleziono maszyny o id: " + equipmentId));
    }
    
    @Test
    @DisplayName("Test updating already deleted farm equipment")
    public void testUpdateDeletedFarmEquipment() throws Exception {
        // Given
        FarmEquipment farmEquipment = entityManager.createQuery(
                "SELECT e from FarmEquipment e WHERE e.isAvailable = :available", FarmEquipment.class)
        .setParameter("available", false)
        .getSingleResult();
        AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest = new AddUpdateFarmEquipmentRequest();
        addUpdateFarmEquipmentRequest.setEquipmentName("Updated Name");
        SecurityContextHolder.clearContext();

        Farm farm = entityManager.find(Farm.class, farmEquipment.getId().getFarmId());
        User currentUser = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.farm.id = :farmId AND u.role.id = :roleId", User.class)
                .setParameter("farmId", farm.getId())
                .setParameter("roleId", 3)  
                .getSingleResult();

        
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    
        // When
        mockMvc.perform(put("/equipment/{equipmentId}", farmEquipment.getId().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addUpdateFarmEquipmentRequest)))
                .andDo(print())
        //then
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.message").value("Wybrany sprzęt już nie istnieje"));
    }

    /*
     *  DELETE /{equipmentId}
     */
    @Test
    @DisplayName("Test deleting farm equipment successfully")
    public void testDeleteFarmEquipmentSuccess() throws Exception {
        // Given
        Integer equipmentId = 1;      
        // When 
        mockMvc.perform(delete("/equipment/{equipmentId}", equipmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie usunięto maszynę"));
    }

    @Test
    @DisplayName("Test deleting already deleted farm equipment")
    public void testDeleteDeletedFarmEquipment() throws Exception {
        // Given
        FarmEquipment farmEquipment = entityManager.createQuery(
                "SELECT e from FarmEquipment e WHERE e.isAvailable = :available", FarmEquipment.class)
        .setParameter("available", false)
        .getSingleResult();
        AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest = new AddUpdateFarmEquipmentRequest();
        addUpdateFarmEquipmentRequest.setEquipmentName("Updated Name");
        SecurityContextHolder.clearContext();

        Farm farm = entityManager.find(Farm.class, farmEquipment.getId().getFarmId());
        User currentUser = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.farm.id = :farmId AND u.role.id = :roleId", User.class)
                .setParameter("farmId", farm.getId())
                .setParameter("roleId", 3)  
                .getSingleResult();

        
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    
        
        // When
        mockMvc.perform(delete("/equipment/{equipmentId}", farmEquipment.getId().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
        //then
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.message").value("Wybrana maszyna została już usunięta"));
    }

    @Test
    @DisplayName("Test deleting non-existent farm equipment")
    public void testDeleteNonExistentFarmEquipment() throws Exception {
        // Given
        Integer nonExistentEquipmentId = 9999; 
        
        // When 
        mockMvc.perform(delete("/equipment/{equipmentId}", nonExistentEquipmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
        //then
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.message").value("Nie znaleziono maszyny o id: " + nonExistentEquipmentId));
    }
    /*
     *  POST /new
     */


    @Test
    @DisplayName("Test successfully adding new farm equipment")
    public void testAddNewFarmEquipmentSuccess() throws Exception {
        // Given
        AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest = new AddUpdateFarmEquipmentRequest();
        addUpdateFarmEquipmentRequest.setEquipmentName("Tractor X");
        addUpdateFarmEquipmentRequest.setCategory("Ciągniki rolnicze");
        addUpdateFarmEquipmentRequest.setBrand("Brand X");
        addUpdateFarmEquipmentRequest.setModel("Model X");
        addUpdateFarmEquipmentRequest.setPower(120);
        addUpdateFarmEquipmentRequest.setWorkingWidth(5.5);
    
        // When
        mockMvc.perform(post("/equipment/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addUpdateFarmEquipmentRequest)))
                .andDo(print())
        //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Pomyślnie dodano nową maszynę"));
    }  

    @Test
    @DisplayName("Test adding farm equipment with duplicate name")
    public void testAddFarmEquipmentDuplicateName() throws Exception {
        // Given
        AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest = new AddUpdateFarmEquipmentRequest();
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1,1);
        FarmEquipment farmEquipment = entityManager.find(FarmEquipment.class,farmEquipmentId );
        addUpdateFarmEquipmentRequest.setEquipmentName(farmEquipment.getEquipmentName());
        addUpdateFarmEquipmentRequest.setCategory("Ciągniki rolnicze");
        
        // When 
        mockMvc.perform(post("/equipment/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addUpdateFarmEquipmentRequest)))
                .andDo(print())
        //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Maszyna o podanej nazwie już istnieje"));
    }
    /*
     *  GET /categories
     */
    @Test 
    @DisplayName("Test retrieving all equipment categories without manually checking every category")
    public void testGetAllCategoriesWithFieldsLargeList() throws Exception { 
        mockMvc.perform(get("/equipment/categories")
              .contentType(MediaType.APPLICATION_JSON))   
              .andDo(print())
              .andExpect(status().isOk()) 
              .andExpect(jsonPath("$.length()").value(61));
}
    
}
