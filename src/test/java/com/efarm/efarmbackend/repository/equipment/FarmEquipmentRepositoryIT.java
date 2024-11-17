package com.efarm.efarmbackend.repository.equipment;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;

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
        assertThat(farmEquipmentsList, not(empty()));
        assertThat(countActive.intValue(), is(farmEquipmentsList.size()));
        assertThat(farmEquipmentsList, everyItem(hasProperty("id", hasProperty("farmId", is(farmId)))));
    }

    @Test
    @DisplayName("Tests that equipment exists in farm by its name")
    public void testEquipmentExistsByNameAndFarmId() {
        //given
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(1,1);
        FarmEquipment farmEquipment = entityManager.find(FarmEquipment.class, farmEquipmentId);
        String equipmentName = farmEquipment.getEquipmentName();
        Farm farm = entityManager.find(Farm.class, farmEquipment.getId().getFarmId());

        //when
        Boolean exists = farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(equipmentName,farm);

        //then
        assertThat(exists, is(true));
    }

    @Test
    @DisplayName("Tests that equipment does not exists in farm by its name")
    public void testEquipmentDoesNotExistByNameAndFarmId() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);

        //when
        Boolean exists = farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm("Nonexistent Equipment", farm);

        //then
        assertThat(exists, is(false));
    }

    @Test
    @DisplayName("Tests that it returns max id for equipment in farm")
    public void testFindMaxEquipmentIdForFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);

        Integer maxId = entityManager.getEntityManager()
        .createQuery("SELECT MAX(e.id.id) FROM FarmEquipment e WHERE e.id.farmId = 1", Integer.class)
        .getSingleResult();

        //when
        Optional<Integer> maxIdFound = farmEquipmentRepository.findMaxIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound.get(),is(maxId));
    }

    @Test
    @DisplayName("Tests that it returns id 1 for equipment in farm that doesnt have any equipment yet")
    public void testFindMaxEquipmentIdForNewFarm() {
        //given
        Address address = new Address();
        entityManager.persist(address);
        entityManager.flush();
        ActivationCode activationCode = entityManager.find(ActivationCode.class, 1);
        Farm farm = new Farm();
        farm.setFarmName("uniqueFarmName");
        farm.setIsActive(true);
        farm.setIdAddress(address.getId());
        farm.setIdActivationCode(activationCode.getId());
        entityManager.persist(farm);
        entityManager.flush();

        //when
        Optional<Integer> maxIdFound = farmEquipmentRepository.findMaxIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound,is(Optional.empty()));
    }

    @Test
    @DisplayName("Tests that it returns next free id for equipment in farm")
    public void testFindNextMaxFreeEquipmentIdForFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);

        Integer maxId = entityManager.getEntityManager()
        .createQuery("SELECT MAX(e.id.id) FROM FarmEquipment e WHERE e.id.farmId = 1", Integer.class)
        .getSingleResult();

        //when
        Integer maxIdFound = farmEquipmentRepository.findNextFreeIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound,is(maxId+1));
    }

    @Test
    @DisplayName("Tests that it returns next free id for equipment in new farm")
    public void testFindNextMaxFreeEquipmentIdForNewFarm() {
        //given
        Address address = new Address();
        entityManager.persist(address);
        entityManager.flush();
        ActivationCode activationCode = entityManager.find(ActivationCode.class, 1);
        Farm farm = new Farm();
        farm.setFarmName("uniqueFarmName");
        farm.setIsActive(true);
        farm.setIdAddress(address.getId());
        farm.setIdActivationCode(activationCode.getId());
        entityManager.persist(farm);
        entityManager.flush();

        //when
        Integer maxIdFound = farmEquipmentRepository.findNextFreeIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound,is(1));
    }

}
