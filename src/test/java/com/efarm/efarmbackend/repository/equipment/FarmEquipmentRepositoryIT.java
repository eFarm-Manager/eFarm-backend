package com.efarm.efarmbackend.repository.equipment;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class FarmEquipmentRepositoryIT {
    @Autowired
    FarmEquipmentRepository farmEquipmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Tests finding all equipment from farm by its id")
    public void testFindAllFarmEquipmentByFarmId() {
        //given
        Integer farmId = 3;

        Long countActive = entityManager.getEntityManager()
            .createQuery("SELECT COUNT(*) FROM FarmEquipment e WHERE e.id.farmId = 3", Long.class)
            .getSingleResult();

        //when
        List<FarmEquipment> farmEquipmentsList = farmEquipmentRepository.findByFarmIdFarm_Id(farmId);
        
        //then
        System.out.println(farmEquipmentsList);
        assertThat(farmEquipmentsList,not(empty()));
        assertThat(countActive.intValue(), is(farmEquipmentsList.size()));
        assertThat(farmEquipmentsList, everyItem(hasProperty("id",hasProperty("farmId", is(farmId)))));

    }
}
