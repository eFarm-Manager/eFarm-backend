package com.efarm.efarmbackend.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class FarmServiceIT {

    @Autowired
    private FarmService farmService;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // in like 2025 its gonna get fucked since all the expired dates in random datas are 2025 but not an issue now i guess
    @Test
    @DisplayName("Tests that all farms with expired activation codes will be not active")
    void testDeactivateFarmsWithExpiredActivationCodes() throws Exception {
        //given
        ActivationCode expiredCode = new ActivationCode();
        expiredCode.setCode("thisCodeIsExpired");
        expiredCode.setExpireDate(LocalDate.now().minusDays(1));
        expiredCode.setIsUsed(false);
        expiredCode.setId(500);

        Address address = new Address();
        address.setId(500);

        Address managedAddress = entityManager.merge(address);
        ActivationCode managedCode = entityManager.merge(expiredCode);
        entityManager.flush();

        Farm farm = new Farm();
        farm.setId(500);
        farm.setFarmName("uniqueFarmName");
        farm.setIdActivationCode(managedCode.getId());
        farm.setIdAddress(managedAddress.getId());
        farm.setIsActive(true);
        Farm managedFarm = entityManager.merge(farm);
        entityManager.flush();

        //when 
        farmService.deactivateFarmsWithExpiredActivationCodes();

        //then
        Farm updatedFarm = entityManager.find(Farm.class, managedFarm.getId());
        assertThat(farm.getIsActive(), is(true));
        assertThat(updatedFarm.getIsActive(), is(false));
        assertThat(updatedFarm.getFarmName(), is(farm.getFarmName()));
    }

}
