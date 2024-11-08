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

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordDTO;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import com.efarm.efarmbackend.model.landparcel.LandparcelId;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.CreateNewAgriculturalRecordRequest;
import com.efarm.efarmbackend.payload.request.agriculturalrecord.UpdateAgriculturalRecordRequest;
import com.efarm.efarmbackend.payload.request.landparcel.AddLandparcelRequest;
import com.efarm.efarmbackend.payload.request.landparcel.UpdateLandparcelRequest;
import com.efarm.efarmbackend.repository.agriculturalrecords.SeasonRepository;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.efarm.efarmbackend.service.agriculturalrecords.SeasonService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
public class AgriculturalRecordControllerIT {

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
     * GET /all
     */

    @Test
    public void shouldReturnAllLandparcels() throws Exception {
        // given
        Long expectedAgriculturalRecordsCount = entityManager.createQuery("SELECT COUNT(ar) FROM AgriculturalRecord ar WHERE ar.season=:season AND ar.id.farmId = :farmId", Long.class)
                .setParameter("season", returnCurrentSeason())
                .setParameter("farmId", 1)
                .getSingleResult();

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/records/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<AgriculturalRecordDTO> agriculturalRecordDTOs = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        assertThat(agriculturalRecordDTOs.size(), is(expectedAgriculturalRecordsCount.intValue()));
    }

    @Test
    public void shouldReturnRecordsForSpecificSeason() throws Exception {
        // given
        String seasonName = "2023/2024";
        Season season = entityManager.createQuery(
                "SELECT s FROM Season s WHERE s.name = :name", Season.class)
                .setParameter("name", seasonName)
                .getSingleResult();

        Long expectedRecordsCount = entityManager.createQuery(
                "SELECT COUNT(ar) FROM AgriculturalRecord ar WHERE ar.season = :season AND ar.id.farmId = :farmId", Long.class)
                .setParameter("season", season)
                .setParameter("farmId", 1)
                .getSingleResult();

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/records/all")
                .param("season", seasonName))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<AgriculturalRecordDTO> records = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});
        assertThat(records.size(), is(expectedRecordsCount.intValue()));
    }
    /*
     * POST /add-new-record
     */

    @Test
    public void shouldAddNewRecord() throws Exception {
        // given
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest();
        request.setSeason(returnCurrentSeason().getName());
        request.setLandparcelId(1);
        request.setArea(5.0);
        request.setCropName("ziemniak");
        request.setDescription("test description");
        
        Landparcel landparcel = entityManager.find(Landparcel.class, new LandparcelId(1, 1));
        double currentArea = landparcel.getArea(); 
        landparcel.setArea(currentArea + 5.0); 
        entityManager.merge(landparcel);

        // when
        mockMvc.perform(post("/api/records/add-new-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Pomyślnie dodano nową uprawę"));
    }

    @Test
    public void shouldThrowExceptionForUnavailableLandparcel() throws Exception {
        // given
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest();
        request.setLandparcelId(1);
        request.setSeason(returnCurrentSeason().getName());
        request.setArea(2.0);
        request.setCropName("ziemniak");
        request.setDescription("test description");

        Landparcel landparcel = entityManager.find(Landparcel.class, new LandparcelId(1, 1));
        double currentArea = landparcel.getArea(); 
        landparcel.setArea(currentArea + 5.0); 
        landparcel.setIsAvailable(false);
        entityManager.merge(landparcel);
    
        // when
        mockMvc.perform(post("/api/records/add-new-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrane pole jest niedostępne!"));
    }

    @Test
    public void shouldThrowExceptionForExceedingLandparcelArea() throws Exception {
        // given
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest();
        request.setSeason(returnCurrentSeason().getName());
        request.setLandparcelId(2);
        request.setArea(1.0); 
        request.setCropName("ziemniak");
        request.setDescription("test description");
    
        // when
        mockMvc.perform(post("/api/records/add-new-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Maksymalna niewykorzystana powierzchnia na tym polu to: 0.0 ha. Spróbuj najpierw zmniejszyć powierzchnię pozostałych upraw.")); 
    }

    @Test
    public void shouldThrowExceptionForInvalidCropName() throws Exception {
        // given
        CreateNewAgriculturalRecordRequest request = new CreateNewAgriculturalRecordRequest();
        request.setSeason(returnCurrentSeason().getName());
        request.setLandparcelId(1);
        request.setArea(2.0);
        request.setCropName("invalid_crop"); 
        request.setDescription("test description");
    
        Landparcel landparcel = entityManager.find(Landparcel.class, new LandparcelId(1, 1));
        double currentArea = landparcel.getArea();
        landparcel.setArea(currentArea + 2.0); 
        entityManager.merge(landparcel);
    
        // when
        mockMvc.perform(post("/api/records/add-new-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrano nieprawidłowy rodzaj uprawy"));
    }
    /*
     * POST /generate-records-for-new-season
     */
    
    @Test
    public void shouldGenerateRecordsForNewSeason() throws Exception {
        // given
        String seasonName = "test season"; 
        Season season = new Season();
        season.setName(seasonName);
        entityManager.persist(season);
        // when
        mockMvc.perform(post("/api/records/generate-records-for-new-season")
                .param("seasonName", seasonName)
                .contentType(MediaType.APPLICATION_JSON))
        // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Ewidencje dla nowego sezonu zostały wygenerowane"));

        List<AgriculturalRecord> records = entityManager.createQuery(
            "SELECT ar FROM AgriculturalRecord ar WHERE ar.season = :season", AgriculturalRecord.class)
                .setParameter("season", season)
                .getResultList();

        assertThat(records.size(), is(greaterThan(0)));
    }

    @Test
    public void shouldReturnBadRequestForNonExistentSeason() throws Exception {
        // given
        String nonExistentSeasonName = "non-existent season"; 
    
        // when
        mockMvc.perform(post("/api/records/generate-records-for-new-season")
                .param("seasonName", nonExistentSeasonName)
                .contentType(MediaType.APPLICATION_JSON))
        // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Podany sezon nie istnieje.")); 
    }

    /*
     * PUT /{id}
     */

    @Test
    public void shouldUpdateRecord() throws Exception {
        // given
        Integer recordId = 1;
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest();
        request.setCropName("ziemniak");
        request.setArea(3.0);
        request.setDescription("updated description");
    
        // when
        mockMvc.perform(put("/api/records/" + recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie zaktualizowano dane"));

        AgriculturalRecord updatedRecord = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(recordId, 1));
        assertThat(updatedRecord.getCrop().getName(), is(request.getCropName()));
        assertThat(updatedRecord.getArea(), is(request.getArea()));
        assertThat(updatedRecord.getDescription(), is(request.getDescription()));
    }

    @Test
    public void shouldReturnBadRequestForNonExistentRecord() throws Exception {
        // given
        Integer nonExistentRecordId = 999; 
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest();
        request.setCropName("ziemniak");
    
        // when
        mockMvc.perform(put("/api/records/" + nonExistentRecordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono ewidencji"));
    }

    @Test
    public void shouldReturnBadRequestForInvalidCropName() throws Exception {
        // given
        Integer recordId = 1;
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest();
        request.setCropName("invalid_crop"); 
    
        // when
        mockMvc.perform(put("/api/records/" + recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wybrano nieprawidłowy rodzaj uprawy")); 
    }
    
    @Test
    public void shouldReturnBadRequestForExceedingArea() throws Exception {
        // given
        Integer recordId = 1;
        UpdateAgriculturalRecordRequest request = new UpdateAgriculturalRecordRequest();
        AgriculturalRecord record = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(recordId, 1));
        request.setArea(record.getArea() + 10.0); 

        // when
        mockMvc.perform(put("/api/records/" + recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Maksymalna niewykorzystana powierzchnia na tym polu to: "+ record.getArea()+" ha. Spróbuj najpierw zmniejszyć powierzchnię pozostałych upraw.")); 
    }

    /*
     * DELETE /{id}
     */
    
    @Test
    public void shouldDeleteRecord() throws Exception {
        // given
        Integer recordId = 1;
    
        // when
        mockMvc.perform(delete("/api/records/" + recordId))
        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie usunięto wskazaną ewidencję"));

        AgriculturalRecord deletedRecord = entityManager.find(AgriculturalRecord.class, new AgriculturalRecordId(recordId, 1));
        assertThat(deletedRecord, is((AgriculturalRecord) null));

        List<AgroActivity> remainingAgroActivities = entityManager.createQuery(
        "SELECT a FROM AgroActivity a WHERE a.agriculturalRecord.id = :recordId", AgroActivity.class)
                .setParameter("recordId", new AgriculturalRecordId(recordId, 1))
                .getResultList();
        assertThat(remainingAgroActivities, is(empty()));
    }

    @Test
    public void shouldReturnBadRequestForNonExistentRecordWhenDeleting() throws Exception {
        // given
        Integer nonExistentRecordId = 999; 
    
        // when
        mockMvc.perform(delete("/api/records/" + nonExistentRecordId))
        // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ewidencja, którą próbujesz usunąć nie istnieje!")); 
    }

    /*
     * GET /available-seasons
     */
    
    @Test
    public void shouldReturnAvailableSeasons() throws Exception {
        // given
        List<String> expectedSeasons = entityManager.createQuery(
                "SELECT s.name FROM Season s", String.class)
                .getResultList();
    
        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/records/available-seasons"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    
        // then
        List<String> seasons = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});
        assertThat(seasons, is(expectedSeasons));
    }
    
    /*
     * GET /available-crops
     */

    @Test
    public void shouldReturnAvailableCrops() throws Exception {
        // given
        List<String> expectedCrops = entityManager.createQuery(
                "SELECT c.name FROM Crop c", String.class)
                .getResultList();
    
        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/records/available-crops"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    
        // then
        List<String> crops = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});
        assertThat(crops, is(expectedCrops));
    }
    
    // Helper method to get current season 

    Season returnCurrentSeason() {
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        String seasonName;
        int month = currentDate.getMonthValue();
        //getting current season into String ex. output 2022/2023
        seasonName = month >= 8 ? year + "/" + (year + 1) : (year - 1) + "/" + year;
        return entityManager.createQuery("SELECT s FROM Season s WHERE s.name=:name", Season.class)
                .setParameter("name", seasonName)
                .getSingleResult();
    }
    
}
