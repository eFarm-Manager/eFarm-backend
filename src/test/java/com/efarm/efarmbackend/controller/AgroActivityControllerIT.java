package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.agroactivity.*;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.agroactivity.NewAgroActivityRequest;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class AgroActivityControllerIT {

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
     * POST /new
     */

    @Test
    public void testAddAgroActivity() throws Exception {
        //given
        ActivityCategory activityCategory = entityManager.find(ActivityCategory.class, 1);

        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName(activityCategory.getName());
        request.setName("Nowy zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setAgriculturalRecordId(1);
        request.setOperatorIds(Arrays.asList(1));
        request.setEquipmentIds(Arrays.asList(1));

        //when
        mockMvc.perform(post("/agro-activities/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Pomyślnie dodano nowy zabieg agrotechniczny"));

        AgroActivity agroActivity = entityManager.createQuery(
                        "SELECT a FROM AgroActivity a WHERE a.name = :name", AgroActivity.class)
                .setParameter("name", "Nowy zabieg")
                .getSingleResult();

        assertThat(agroActivity, is(notNullValue()));
        assertThat(agroActivity.getName(), is("Nowy zabieg"));
        assertThat(agroActivity.getActivityCategory().getName(), is(activityCategory.getName()));
        assertThat(agroActivity.getAgriculturalRecord().getId().getId(), is(1));
        assertThat(agroActivity.getDate(), is(request.getDate()));
        assertThat(agroActivity.getIsCompleted(), is(true));
        assertThat(agroActivity.getUsedSubstances(), is(""));
        assertThat(agroActivity.getAppliedDose(), is(""));
        assertThat(agroActivity.getDescription(), is(""));
    }

    @Test
    public void testAddAgroActivityWithNonExistentCategory() throws Exception {
        //given
        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName("NonExistentCategory");
        request.setName("Nowy zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setAgriculturalRecordId(1);
        request.setOperatorIds(Arrays.asList(1));
        request.setEquipmentIds(Arrays.asList(1));

        //when
        mockMvc.perform(post("/agro-activities/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono kategorii zabiegu"));
    }

    @Test
    public void testAddAgroActivityWithNonExistentAgriculturalRecord() throws Exception {
        //given
        ActivityCategory activityCategory = entityManager.find(ActivityCategory.class, 1);

        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName(activityCategory.getName());
        request.setName("Nowy zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setAgriculturalRecordId(999); // Non-existent agricultural record ID
        request.setOperatorIds(Arrays.asList(1));
        request.setEquipmentIds(Arrays.asList(1));

        //when
        mockMvc.perform(post("/agro-activities/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono ewidencji"));
    }

    @Test
    public void testAddAgroActivityWithNonExistentOperator() throws Exception {
        //given
        ActivityCategory activityCategory = entityManager.find(ActivityCategory.class, 1);

        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName(activityCategory.getName());
        request.setName("Nowy zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setAgriculturalRecordId(1);
        request.setOperatorIds(Arrays.asList(999)); // Non-existent operator ID
        request.setEquipmentIds(Arrays.asList(1));

        //when
        mockMvc.perform(post("/agro-activities/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono użytkownika o ID: 999"));
    }

    @Test
    public void testAddAgroActivityWithNonExistentEquipment() throws Exception {
        //given
        ActivityCategory activityCategory = entityManager.find(ActivityCategory.class, 1);

        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName(activityCategory.getName());
        request.setName("Nowy zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setAgriculturalRecordId(1);
        request.setOperatorIds(Arrays.asList(1));
        request.setEquipmentIds(Arrays.asList(999)); // Non-existent equipment ID

        //when
        mockMvc.perform(post("/agro-activities/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sprzęty o następujących identyfikatorach nie istnieją: [999]"));
    }

    @Test
    public void testAddAgroActivityWithOperatorThatDoesntBelongInCurrentFarm() throws Exception {
        //given
        ActivityCategory activityCategory = entityManager.find(ActivityCategory.class, 1);

        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName(activityCategory.getName());
        request.setName("Nowy zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setAgriculturalRecordId(1);
        request.setOperatorIds(Arrays.asList(2)); // Operator from different farm
        request.setEquipmentIds(Arrays.asList(1));

        //when
        mockMvc.perform(post("/agro-activities/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Użytkownik o ID: 2 nie należy do tej farmy"));
    }

    @Test
    public void testAddAgroActivityWithOperatorThatIsInactive() throws Exception {
        //given
        User inactivUser = new User("fisrtName", "lastName", "testInactiveUser", "email@gmai.com", "pass123", "");
        inactivUser.setIsActive(false);
        inactivUser.setFarm(entityManager.find(Farm.class, 1));
        inactivUser.setRole(entityManager.find(Role.class, 1));
        entityManager.persist(inactivUser);

        ActivityCategory activityCategory = entityManager.find(ActivityCategory.class, 1);

        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName(activityCategory.getName());
        request.setName("Nowy zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setAgriculturalRecordId(1);
        request.setOperatorIds(Arrays.asList(inactivUser.getId())); // Inactive operator
        request.setEquipmentIds(Arrays.asList(1));

        //when
        mockMvc.perform(post("/agro-activities/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Użytkownik fisrtName lastName jest niedostępny"));
    }

    /*
     * GET /{id}
     */

    @Test
    public void testGetAgroActivitiesByRecord() throws Exception {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1, 1));
        Integer agriculturalRecordId = agroActivity.getAgriculturalRecord().getId().getId();

        //when
        MvcResult result = mockMvc.perform(get("/agro-activities/{id}", agriculturalRecordId)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        List<AgroActivitySummaryDTO> agroActivities = objectMapper.readValue(jsonResponse, new TypeReference<List<AgroActivitySummaryDTO>>() {
        });

        assertThat(agroActivities, is(not(empty())));
        assertThat(agroActivities.size(), is(greaterThan(0)));

        AgroActivitySummaryDTO firstActivity = agroActivities.get(0);
        assertThat(firstActivity.getName(), is(notNullValue()));
        assertThat(firstActivity.getDate(), is(notNullValue()));
        assertThat(firstActivity.getIsCompleted(), is(notNullValue()));
        assertThat(firstActivity.getCategoryName(), is(notNullValue()));
    }

    @Test
    public void testGetAgroActivitiesByRecordWithNonExistentRecord() throws Exception {
        //given
        Integer nonExistentRecordId = 999;

        //when
        mockMvc.perform(get("/agro-activities/{id}", nonExistentRecordId)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono ewidencji"));
    }

    /*
     * GET /details/{id}
     */

    @Test
    public void testGetAgroActivityDetails() throws Exception {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1, 1));

        //when
        MvcResult result = mockMvc.perform(get("/agro-activities/details/{id}", agroActivity.getId().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        String jsonResponse = result.getResponse().getContentAsString();
        AgroActivityDetailDTO agroActivityDetail = objectMapper.readValue(jsonResponse, AgroActivityDetailDTO.class);

        assertThat(agroActivityDetail.getId(), is(agroActivity.getId().getId()));
        assertThat(agroActivityDetail.getName(), is(agroActivity.getName()));
        assertThat(agroActivityDetail.getDate(), is(agroActivity.getDate()));
        assertThat(agroActivityDetail.getIsCompleted(), is(agroActivity.getIsCompleted()));
        assertThat(agroActivityDetail.getCategoryName(), is(agroActivity.getActivityCategory().getName()));
        assertThat(agroActivityDetail.getUsedSubstances(), is(agroActivity.getUsedSubstances()));
        assertThat(agroActivityDetail.getAppliedDose(), is(agroActivity.getAppliedDose()));
        assertThat(agroActivityDetail.getDescription(), is(agroActivity.getDescription()));
        assertThat(agroActivityDetail.getArea(), is(agroActivity.getAgriculturalRecord().getArea()));
        assertThat(agroActivityDetail.getLandparcel(), notNullValue());
        assertThat(agroActivityDetail.getOperators().size(), is(greaterThan(0)));
        assertThat(agroActivityDetail.getEquipment().size(), is(greaterThan(0)));
    }

    @Test
    public void testGetAgroActivityDetailsWithNonExistentActivity() throws Exception {
        //given
        Integer nonExistentActivityId = 999;

        //when
        mockMvc.perform(get("/agro-activities/details/{id}", nonExistentActivityId)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono zabiegu agrotechnicznego"));
    }

    /*
     * PUT /{id}
     */

    @Test
    public void testUpdateAgroActivity() throws Exception {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1, 1));
        ActivityCategory activityCategory = entityManager.find(ActivityCategory.class, 1);

        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName(activityCategory.getName());
        request.setName("Zaktualizowany zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setOperatorIds(Arrays.asList(1));
        request.setEquipmentIds(Arrays.asList(1));

        //when
        mockMvc.perform(put("/agro-activities/{id}", agroActivity.getId().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie zaktualizowano zabieg agrotechniczny"));

        AgroActivity updatedAgroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1, 1));
        assertThat(updatedAgroActivity.getName(), is("Zaktualizowany zabieg"));
        assertThat(updatedAgroActivity.getActivityCategory().getName(), is(activityCategory.getName()));
        assertThat(updatedAgroActivity.getDate(), is(request.getDate()));
    }

    @Test
    public void testUpdateAgroActivityWithNonExistentId() throws Exception {
        //given
        Integer nonExistentActivityId = 999;
        ActivityCategory activityCategory = entityManager.find(ActivityCategory.class, 1);

        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName(activityCategory.getName());
        request.setName("Zaktualizowany zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setOperatorIds(Arrays.asList(1));
        request.setEquipmentIds(Arrays.asList(1));

        //when
        mockMvc.perform(put("/agro-activities/{id}", nonExistentActivityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono zabiegu agrotechnicznego"));
    }

    @Test
    public void testUpdateAgroActivityWithNonExistentCategory() throws Exception {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1, 1));

        NewAgroActivityRequest request = new NewAgroActivityRequest();
        request.setActivityCategoryName("NonExistentCategory");
        request.setName("Zaktualizowany zabieg");
        request.setDate(Instant.now());
        request.setIsCompleted(true);
        request.setUsedSubstances("");
        request.setAppliedDose("");
        request.setDescription("");
        request.setOperatorIds(Arrays.asList(1));
        request.setEquipmentIds(Arrays.asList(1));

        //when
        mockMvc.perform(put("/agro-activities/{id}", agroActivity.getId().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono kategorii zabiegu"));
    }

    /*
     * DELETE /{id}
     */

    @Test
    public void testDeleteAgroActivity() throws Exception {
        //given
        AgroActivity agroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1, 1));

        //when
        mockMvc.perform(delete("/agro-activities/{id}", agroActivity.getId().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie usunięto zabieg agrotechniczny"));

        AgroActivity deletedAgroActivity = entityManager.find(AgroActivity.class, new AgroActivityId(1, 1));
        assertThat(deletedAgroActivity, is(nullValue()));
    }

    @Test
    public void testDeleteAgroActivityWithNonExistentId() throws Exception {
        //given
        Integer nonExistentActivityId = 999;

        //when
        mockMvc.perform(delete("/agro-activities/{id}", nonExistentActivityId)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono zabiegu agrotechnicznego o ID: 999"));
    }

    /*
     * GET /assigned
     */

    @Test
    public void testGetAssignedIncompleteAgroActivitiesList() throws Exception {
        //when
        MvcResult result = mockMvc.perform(get("/agro-activities/assigned")
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<AgroActivitySummaryDTO> agroActivities = objectMapper.readValue(jsonResponse, new TypeReference<List<AgroActivitySummaryDTO>>() {
        });
        assertThat(agroActivities, is(not(empty())));
        assertThat(agroActivities.size(), is(greaterThan(0)));
        assertThat(agroActivities, everyItem(hasProperty("isCompleted", is(false))));
    }

    /*
     * PATCH /complete/{activityId}
     */

    @Test
    public void testCompleteAgroActivity() throws Exception {
        //given
        Integer farmId = 1, userId = 1;
        AgroActivity incompleteActivity = entityManager.createQuery(
                        "select aa from AgroActivity aa join ActivityHasOperator aho on aa.id = aho.agroActivity.id where aa.id.farmId = :farmId and aa.isCompleted = false and aho.user.id = :userId", AgroActivity.class)
                .setParameter("farmId", farmId)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getSingleResult();

        //when
        mockMvc.perform(patch("/agro-activities/complete/" + incompleteActivity.getId().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Zadanie zostało oznaczone jako zakończone"));

        AgroActivity completedActivity = entityManager.find(AgroActivity.class, new AgroActivityId(incompleteActivity.getId().getId(), farmId));

        assertThat(completedActivity.getIsCompleted(), is(true));
    }

    @Test
    public void testCompleteAgroActivityWithNonExistentId() throws Exception {
        //given
        Integer nonExistentActivityId = 999;

        //when
        mockMvc.perform(patch("/agro-activities/complete/" + nonExistentActivityId)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono zadania"));
    }

    @Test
    public void testCompleteAgroActivityThatDoesntBelongToUser() throws Exception {
        //given
        Integer farmId = 1;
        User user = entityManager.createQuery("select u from User u where u.farm.id = :farmId and u.id != 1", User.class)
                .setParameter("farmId", farmId)
                .setMaxResults(1)
                .getSingleResult();

        AgroActivity incompleteActivity = new AgroActivity();
        incompleteActivity.setId(new AgroActivityId(999, farmId));
        incompleteActivity.setFarm(entityManager.find(Farm.class, farmId));
        incompleteActivity.setIsCompleted(false);
        incompleteActivity.setName("Incomplete Activity");
        incompleteActivity.setDate(Instant.now());
        incompleteActivity.setActivityCategory(entityManager.find(ActivityCategory.class, 1));
        incompleteActivity.setAgriculturalRecord(entityManager.find(AgroActivity.class, new AgroActivityId(1, farmId)).getAgriculturalRecord());
        entityManager.persist(incompleteActivity);

        // Create an ActivityHasOperator to link the activity to the new user
        ActivityHasOperator activityHasOperator = new ActivityHasOperator();
        activityHasOperator.setAgroActivity(incompleteActivity);
        activityHasOperator.setUser(user);
        activityHasOperator.setFarmId(farmId);
        entityManager.persist(activityHasOperator);

        entityManager.flush();

        //when
        mockMvc.perform(patch("/agro-activities/complete/" + incompleteActivity.getId().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie masz dostępu do tego zadania"));
    }

    @Test
    public void testCompleteAgroActivityThatIsAlreadyCompleted() throws Exception {
        //given
        Integer farmId = 1;
        Integer userId = 1;
        AgroActivity completedActivity = entityManager.createQuery(
                        "select aa from AgroActivity aa join ActivityHasOperator aho on aa.id = aho.agroActivity.id where aa.id.farmId = :farmId and aa.isCompleted = true and aho.user.id = :userId", AgroActivity.class)
                .setParameter("farmId", farmId)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getSingleResult();

        //when
        mockMvc.perform(patch("/agro-activities/complete/" + completedActivity.getId().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie masz dostępu do tego zadania"));
    }

    /*
     * GET /available-category
     */

    @Test
    public void testGetAvailableCategories() throws Exception {
        //given
        Integer expectedCategoriesCount = entityManager.createQuery(
                        "SELECT COUNT(c) FROM ActivityCategory c", Long.class)
                .getSingleResult()
                .intValue();
        //when
        MvcResult result = mockMvc.perform(get("/agro-activities/available-category")
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<String> categoriesList = objectMapper.readValue(jsonResponse, new TypeReference<List<String>>() {
        });

        assertThat(categoriesList.size(), is(expectedCategoriesCount));
    }
}
