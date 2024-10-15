package com.efarm.efarmbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentShortDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import com.efarm.efarmbackend.model.landparcel.LandparcelId;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.landparcel.AddLandparcelRequest;
import com.efarm.efarmbackend.payload.request.landparcel.UpdateLandparcelRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.efarm.efarmbackend.service.landparcel.LandparcelFacade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class LandparcelControllerIT {
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
     *  POST /new
     */

    @Test
    void testAddingNewLandparcel() throws Exception {
        //given
        LandparcelDTO landparcelDTO = new LandparcelDTO();
        landparcelDTO.setLandOwnershipStatus("STATUS_PRIVATELY_OWNED"); 
        landparcelDTO.setVoivodeship("Lubelskie");
        landparcelDTO.setDistrict("district");
        landparcelDTO.setCommune("commune");
        landparcelDTO.setGeodesyRegistrationDistrictNumber("GRD1");
        landparcelDTO.setLandparcelNumber("LP1");
        landparcelDTO.setLongitude(21.0122);
        landparcelDTO.setLatitude(52.2297);
        landparcelDTO.setArea(500.0);

        //when
        mockMvc.perform(post("/api/landparcel/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(landparcelDTO)))
        //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Pomyślnie dodano nową działkę"));
    }

    @Test
    void testAddingExistingLandparcel() throws Exception {
        //given
        LandparcelId landparcelId = new LandparcelId(1, 1);
        Landparcel existingLandparcel = entityManager.find(Landparcel.class, landparcelId);

        AddLandparcelRequest addLandparcelRequest = new AddLandparcelRequest();
        addLandparcelRequest.setLandOwnershipStatus(existingLandparcel.getLandOwnershipStatus().getOwnershipStatus().toString());
        addLandparcelRequest.setVoivodeship(existingLandparcel.getVoivodeship());
        addLandparcelRequest.setDistrict(existingLandparcel.getDistrict());
        addLandparcelRequest.setCommune(existingLandparcel.getCommune());
        addLandparcelRequest.setGeodesyRegistrationDistrictNumber(existingLandparcel.getGeodesyRegistrationDistrictNumber());
        addLandparcelRequest.setLandparcelNumber(existingLandparcel.getLandparcelNumber());
        addLandparcelRequest.setLongitude(existingLandparcel.getLongitude());
        addLandparcelRequest.setLatitude(existingLandparcel.getLatitude());
        addLandparcelRequest.setArea(existingLandparcel.getArea());

        //when
        mockMvc.perform(post("/api/landparcel/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addLandparcelRequest)))
        //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Działka o powyższych danych geodezyjnych już istnieje!"));
    }

    /*
     * GET /{landparcelId}
     */

    @Test
    void testReturningLandparcelDetails() throws Exception {
        //given 
        LandparcelId landparcelId = new LandparcelId(1, 1);
        Landparcel firstLandparcel = entityManager.find(Landparcel.class, landparcelId);


        //when
        MvcResult result = mockMvc.perform(get("/api/landparcel/1"))
        //then
            .andExpect(status().isOk())
            .andReturn();
            
            
        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        LandparcelDTO landparcelDTO = objectMapper.readValue(jsonResponse, new TypeReference<LandparcelDTO>() {});
        assertThat(landparcelDTO.getId(), is(firstLandparcel.getId().getId()));
        assertThat(landparcelDTO.getLandOwnershipStatus(), is(firstLandparcel.getLandOwnershipStatus().getOwnershipStatus().toString()));
        assertThat(landparcelDTO.getDistrict(), is(firstLandparcel.getDistrict()));
        assertThat(landparcelDTO.getCommune(),is(firstLandparcel.getCommune()));
        assertThat(landparcelDTO.getGeodesyRegistrationDistrictNumber(), is(firstLandparcel.getGeodesyRegistrationDistrictNumber()));
        assertThat(landparcelDTO.getLandparcelNumber(),is(firstLandparcel.getLandparcelNumber()));
    }
    /*
     * PUT /landparcelId
     */

    @Test
    void testUpdateLandparcelWithValidRequest() throws Exception {
        //given
        LandparcelId landparcelId = new LandparcelId(1, 1);
 
        UpdateLandparcelRequest updateRequest = new UpdateLandparcelRequest();
        updateRequest.setLandOwnershipStatus("STATUS_PRIVATELY_OWNED");
        updateRequest.setLongitude(89.0122);
        updateRequest.setLatitude(1.2297);
        updateRequest.setArea(999.0);

        //when
        mockMvc.perform(put("/api/landparcel/" + landparcelId.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateRequest)))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Dane działki zostały pomyślnie zaktualizowane"));
    }
    
    @Test
    void testUpdateNonExistentLandparcel() throws Exception {
        //given
        Integer nonExistentId = 999;
        UpdateLandparcelRequest updateRequest = new UpdateLandparcelRequest();
        updateRequest.setLandOwnershipStatus("STATUS_PRIVATELY_OWNED");
        updateRequest.setLongitude(89.0122);
        updateRequest.setLatitude(1.2297);
        updateRequest.setArea(999.0);
    
        //when
        mockMvc.perform(put("/api/landparcel/" + nonExistentId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateRequest)))
        //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Działka nie istnieje"));
    }

    @Test
    void testUpdateLandparcelWhenNotAvailable() throws Exception {
        //given
        LandparcelId landparcelId = new LandparcelId(1, 1);
        Landparcel landparcel = entityManager.find(Landparcel.class, landparcelId);
        landparcel.setIsAvailable(false);  
        entityManager.persist(landparcel);
    
        UpdateLandparcelRequest updateRequest = new UpdateLandparcelRequest();
        updateRequest.setLandOwnershipStatus("STATUS_PRIVATELY_OWNED");
        updateRequest.setLongitude(89.0122);
        updateRequest.setLatitude(1.2297);
        updateRequest.setArea(999.0);
    
        //when
        mockMvc.perform(put("/api/landparcel/" + landparcelId.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateRequest)))
        //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrana działka już nie istnieje"));
    }
    /*
     * DELETE /{landparcelId}
     */

    @Test
    void testDeleteLandparcel() throws Exception {
        //given
        LandparcelId landparcelId = new LandparcelId(1, 1);

        //when
        mockMvc.perform(delete("/api/landparcel/" + landparcelId.getId())
                .contentType(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Działka została pomyślnie usunięta"));

        Landparcel deletedLandparcel = entityManager.find(Landparcel.class, landparcelId);
        assertThat(deletedLandparcel.getIsAvailable(), is(false));
    }

    @Test
    void testDeleteNonExistentLandparcel() throws Exception {
        //given
        Integer nonExistentLandparcelId = 999; 
    
        //when
        mockMvc.perform(delete("/api/landparcel/" + nonExistentLandparcelId)
                .contentType(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono działki o id: " + nonExistentLandparcelId));
    }

    @Test
    void testDeleteAlreadyDeletedLandparcel() throws Exception {
        //given
        LandparcelId landparcelId = new LandparcelId(1, 1);
        Landparcel existingLandparcel = entityManager.find(Landparcel.class, landparcelId);
        
        existingLandparcel.setIsAvailable(false);
        entityManager.persist(existingLandparcel);
    
        //when
        mockMvc.perform(delete("/api/landparcel/" + landparcelId.getId())
                .contentType(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrana działka już nie istnieje!"));
    }
    /*
     * GET /all
     */

    @Test
    @DisplayName("Test return all land parcels from the same farm")
    void testReturnAllLandparcels() throws Exception {
        //given
        String searchString = null;
        Double minArea = null;
        Double maxArea = null;
    
        Farm currentFarm = entityManager.find(Farm.class, 1);
        Long landparcelCount = entityManager.createQuery(
                        "SELECT COUNT(lp) FROM Landparcel lp WHERE lp.id.farmId = :farmId", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .getSingleResult();
    
        //when
        MvcResult result = mockMvc.perform(get("/api/landparcel/all")
                        .param("searchString", searchString)
                        .param("minArea", minArea != null ? String.valueOf(minArea) : "")
                        .param("maxArea", maxArea != null ? String.valueOf(maxArea) : ""))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    
        //then
        List<LandparcelDTO> landparcelDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<LandparcelDTO>>() {
                });
    
        assertThat(landparcelDTOs.size(), is(landparcelCount.intValue()));
    }

    @Test
    @DisplayName("Test return land parcels with minimum area filtering")
    void testReturnLandparcelsWithMinArea() throws Exception {
        // given
        Farm currentFarm = entityManager.find(Farm.class, 1);
        
        LandparcelId landparcelId = new LandparcelId(1, 1); // Adjust the ID as needed
        Landparcel existingLandparcel = entityManager.find(Landparcel.class, landparcelId);
        
        Double minArea = existingLandparcel.getArea() - 1; 
    
        Long landparcelCount = entityManager.createQuery(
                        "SELECT COUNT(lp) FROM Landparcel lp WHERE lp.id.farmId = :farmId AND lp.area >= :minArea", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .setParameter("minArea", minArea)
                .getSingleResult();
    
        // when
        MvcResult result = mockMvc.perform(get("/api/landparcel/all")
                        .param("minArea", String.valueOf(minArea)))
                .andExpect(status().isOk())
                .andReturn();
    
        // then
        List<LandparcelDTO> landparcelDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<LandparcelDTO>>() {
                });
    
        assertThat(landparcelDTOs.size(), is(landparcelCount.intValue()));
    }

    @Test
    @DisplayName("Test return land parcels with maximum area filtering")
    void testReturnLandparcelsWithMaxArea() throws Exception {
        // given
        Farm currentFarm = entityManager.find(Farm.class, 1);
        
        LandparcelId landparcelId = new LandparcelId(1, 1); 
        Landparcel existingLandparcel = entityManager.find(Landparcel.class, landparcelId);
        
        Double maxArea = existingLandparcel.getArea() + 1; 
    
        Long landparcelCount = entityManager.createQuery(
                        "SELECT COUNT(lp) FROM Landparcel lp WHERE lp.id.farmId = :farmId AND lp.area <= :maxArea", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .setParameter("maxArea", maxArea)
                .getSingleResult();
    
        // when
        MvcResult result = mockMvc.perform(get("/api/landparcel/all")
                        .param("maxArea", String.valueOf(maxArea)))
                .andExpect(status().isOk())
                .andReturn();
    
        // then
        List<LandparcelDTO> landparcelDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<LandparcelDTO>>() {
                });
    
        assertThat(landparcelDTOs.size(), is(landparcelCount.intValue()));
    }

    @Test
    @DisplayName("Test return land parcels with search string filtering")
    void testReturnLandparcelsWithSearchString() throws Exception {
        // given
        Farm currentFarm = entityManager.find(Farm.class, 1);
        
        LandparcelId landparcelId = new LandparcelId(1, 1); 
        Landparcel existingLandparcel = entityManager.find(Landparcel.class, landparcelId);
        
        String searchString = existingLandparcel.getCommune(); 
    
        Long landparcelCount = entityManager.createQuery(
                        "SELECT COUNT(lp) FROM Landparcel lp WHERE lp.id.farmId = :farmId AND lp.commune LIKE :searchString", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .setParameter("searchString", "%" + searchString + "%")
                .getSingleResult();
    
        // when
        MvcResult result = mockMvc.perform(get("/api/landparcel/all")
                        .param("searchString", searchString))
                .andExpect(status().isOk())
                .andReturn();
    
        // then
        List<LandparcelDTO> landparcelDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<LandparcelDTO>>() {
                });
    
        assertThat(landparcelDTOs.size(), is(landparcelCount.intValue()));
    }

    @Test
    @DisplayName("Test return land parcels with combined filtering")
    void testReturnLandparcelsWithCombinedFiltering() throws Exception {
        // given
        Farm currentFarm = entityManager.find(Farm.class, 1);
        
        LandparcelId landparcelId = new LandparcelId(1, 1); 
        Landparcel existingLandparcel = entityManager.find(Landparcel.class, landparcelId);
        
        Double minArea = existingLandparcel.getArea() - 100; 
        Double maxArea = existingLandparcel.getArea() + 100; 
        String searchString = existingLandparcel.getCommune(); 
    
        Long landparcelCount = entityManager.createQuery(
                        "SELECT COUNT(lp) FROM Landparcel lp WHERE lp.id.farmId = :farmId AND lp.area BETWEEN :minArea AND :maxArea AND lp.commune LIKE :searchString", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .setParameter("minArea", minArea)
                .setParameter("maxArea", maxArea)
                .setParameter("searchString", "%" + searchString + "%")
                .getSingleResult();
    
        // when
        MvcResult result = mockMvc.perform(get("/api/landparcel/all")
                        .param("searchString", searchString)
                        .param("minArea", String.valueOf(minArea))
                        .param("maxArea", String.valueOf(maxArea)))
                .andExpect(status().isOk())
                .andReturn();
    
        // then
        List<LandparcelDTO> landparcelDTOs = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<LandparcelDTO>>() {
                });
    
        assertThat(landparcelDTOs.size(), is(landparcelCount.intValue()));
    }
    

}
    