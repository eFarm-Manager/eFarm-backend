package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus;
import com.efarm.efarmbackend.model.landparcel.LandOwnershipStatus;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelId;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.response.CropStatisticsResponse;
import com.efarm.efarmbackend.payload.response.LandAreaStatisticsResponse;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class FarmStatisticsControllerIT {
    
    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Farm testFarm;

    @BeforeEach
    public void setupTestUserAndFarm() {
        if (testUser == null) { 
            Address testAddress = new Address();

            entityManager.persist(testAddress);
            entityManager.flush();

            testFarm = new Farm();
            testFarm.setIdActivationCode(4);
            testFarm.setIdAddress(testAddress.getId());
            testFarm.setFarmName("Test Farm");
            testFarm.setIsActive(true);

            entityManager.persist(testFarm);
            entityManager.flush();

            testUser = new User();
            testUser.setIsActive(true);
            testUser.setFirstName("test first name");
            testUser.setLastName("test last name");
            testUser.setEmail("testEmail@gmail.com");
            testUser.setUsername("test_user");
            testUser.setPassword("test_password"); 
            testUser.setFarm(testFarm);

            Role ownerRole = entityManager.createQuery("select r from Role r where r.name = :name", Role.class)
            .setParameter("name", ERole.ROLE_FARM_OWNER)
            .getSingleResult();
            testUser.setRole(ownerRole);

            entityManager.persist(testUser);
            entityManager.flush();

            createLandParcelsForFarm(testFarm.getFarmName());
            createAgriculturalRecordsForFarm(testFarm.getFarmName());

            UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private void createLandParcelsForFarm(String farmName) {
        Farm managedFarm = entityManager.createQuery("select f from Farm f where f.farmName = :farmName", Farm.class)
                .setParameter("farmName", farmName)
                .getSingleResult();

        LandparcelId landparcelId1 = new LandparcelId(1, managedFarm.getId());
        Landparcel parcel1 = new Landparcel(landparcelId1,managedFarm);
        parcel1.setArea(50.258);
        parcel1.setLatitude(50.258);
        parcel1.setLongitude(50.258);
        parcel1.setName(farmName + " parcel 1");
        parcel1.setCommune(farmName + " commune");
        parcel1.setDistrict(farmName + " district");
        parcel1.setVoivodeship(farmName + " voivodeship");
        parcel1.setGeodesyDistrictNumber("123");
        parcel1.setLandparcelNumber("123");
        parcel1.setGeodesyLandparcelNumber("123");

        LandOwnershipStatus ownershipStatusOwned = entityManager.createQuery("select os from LandOwnershipStatus os where os.ownershipStatus = :ownershipStatus", LandOwnershipStatus.class)
                .setParameter("ownershipStatus", ELandOwnershipStatus.STATUS_PRIVATELY_OWNED)
                .getSingleResult();

        parcel1.setLandOwnershipStatus(ownershipStatusOwned);
        entityManager.persist(parcel1);

        LandparcelId landparcelId2 = new LandparcelId(2, managedFarm.getId());
        Landparcel parcel2 = new Landparcel(landparcelId2,managedFarm);
        parcel2.setArea(3.362);
        parcel2.setLatitude(3.362);
        parcel2.setLongitude(3.362);
        parcel2.setName(farmName + " parcel 2");
        parcel2.setCommune(farmName + " commune 2");
        parcel2.setDistrict(farmName + " district 2");
        parcel2.setVoivodeship(farmName + " voivodeship 2");
        parcel2.setGeodesyDistrictNumber("456");
        parcel2.setLandparcelNumber("456");
        parcel2.setGeodesyLandparcelNumber("456");
        
        LandOwnershipStatus ownershipStatusLeased = entityManager.createQuery("select os from LandOwnershipStatus os where os.ownershipStatus = :ownershipStatus", LandOwnershipStatus.class)
        .setParameter("ownershipStatus", ELandOwnershipStatus.STATUS_LEASE)
        .getSingleResult();

        parcel2.setLandOwnershipStatus(ownershipStatusLeased);
        entityManager.persist(parcel2);

        entityManager.flush(); 
    }

    private void createAgriculturalRecordsForFarm(String farmName) {
        Farm managedFarm = entityManager.createQuery("select f from Farm f where f.farmName = :farmName", Farm.class)
                .setParameter("farmName", farmName)
                .getSingleResult();

        LandparcelId landparcelId1 = new LandparcelId(1, managedFarm.getId());
        Landparcel parcel1 = entityManager.find(Landparcel.class, landparcelId1);

        AgriculturalRecordId recordId1 = new AgriculturalRecordId(1, managedFarm.getId());
        AgriculturalRecordId recordId2 = new AgriculturalRecordId(2, managedFarm.getId());
        Season season = entityManager.createQuery("select s from Season s where s.name = :name", Season.class)
                .setParameter("name", "2024/2025")
                .getSingleResult();

        double area1 = parcel1.getArea()*2/3;
        double area2 = parcel1.getArea()/3;
        Crop currentCrop1 = entityManager.find(Crop.class, 1);
        Crop currentCrop2 = entityManager.find(Crop.class, 2);
        AgriculturalRecord agriculturalRecord1 = new AgriculturalRecord(
            recordId1,
            season,
            parcel1,
            currentCrop1,
            area1,
            managedFarm,
            null
        );
        entityManager.persist(agriculturalRecord1);

        AgriculturalRecord agriculturalRecord2 = new AgriculturalRecord(
            recordId2,
            season,
            parcel1,
            currentCrop2,
            area2,
            managedFarm,
            null
        );
        entityManager.persist(agriculturalRecord2);

        entityManager.flush();
    }

    /*
     * GET /land-area
     */

    @Test
    public void testGetLandArea() throws Exception {
        //given

        //when
        MvcResult mvcResult = mockMvc.perform(get("/statistics/land-area"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        //then
        String content = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        LandAreaStatisticsResponse response = objectMapper.readValue(content, LandAreaStatisticsResponse.class);
        assertThat(response.getTotalAvailableArea(), is(53.62)); // 50.258 + 3.362
        assertThat(response.getPrivatelyOwnedArea(), is(50.258)); 
        assertThat(response.getLeaseArea(), is(3.362));
    }

    /*
     * GET /crop-area
     */

    @Test
    public void testGetCropArea() throws Exception {
        //given

        //when
        MvcResult mvcResult = mockMvc.perform(get("/statistics/crop-area"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        //then
        String content = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<CropStatisticsResponse> response = objectMapper.readValue(content, new TypeReference<List<CropStatisticsResponse>>() {});
        assertThat(response.size(), is(2));
        assertThat(response.get(0).getCropName(), notNullValue());
        assertThat(response.get(0).getTotalArea(), is(33.5053)); // 50.258*2/3 + ToFourDecimal
        assertThat(response.get(1).getCropName(), notNullValue());
        assertThat(response.get(1).getTotalArea(), is(16.7527)); // 50.258*/3 + ToFourDecimal
    }
}
