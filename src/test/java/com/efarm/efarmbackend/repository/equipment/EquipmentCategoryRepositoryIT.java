package com.efarm.efarmbackend.repository.equipment;

import com.efarm.efarmbackend.model.equipment.EquipmentCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class EquipmentCategoryRepositoryIT {

    @Autowired
    EquipmentCategoryRepository equipmentCategoryRepository;

    @Test
    @DisplayName("Tests finding all category names")
    public void testFindAllCategoryNames() {
        //when
        List<String> allCategory = equipmentCategoryRepository.findAllCategoryNames();

        //then
        assertThat(allCategory.size(), is(61));
    }

    @Test
    @DisplayName("Tests finding category by its name")
    public void testFindByCategoryName() {
        //given
        String categoryName = "Ciągniki rolnicze";

        //when
        EquipmentCategory foundCategory = equipmentCategoryRepository.findByCategoryName(categoryName);

        //then
        assertThat(foundCategory, is(notNullValue()));
        assertThat(foundCategory.getCategoryName(), is("Ciągniki rolnicze"));
    }

    @Test
    @DisplayName("Tests not finding category by its name")
    public void testotFindByCategoryName() {
        //given
        String categoryName = "doesnt exist";

        //when
        EquipmentCategory foundCategory = equipmentCategoryRepository.findByCategoryName(categoryName);

        //then
        assertThat(foundCategory, is(nullValue()));
    }


}
