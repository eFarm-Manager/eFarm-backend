package com.efarm.efarmbackend.repository.user;

import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class RoleRepositoryIT {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Tests that operator role is present")
    public void testFindsOperatorRoleByName() {
        //given
        ERole nameTest = ERole.ROLE_FARM_EQUIPMENT_OPERATOR;

        //when
        Optional<Role> foundRole = roleRepository.findByName(nameTest);

        //then
        assertThat(foundRole.isPresent(), is(true));
        assertThat(foundRole.get(), notNullValue());
        assertThat(foundRole.get().getName(), is(nameTest));
    }

    @Test
    @DisplayName("Tests that manager role is present")
    public void testFindsManagerRoleByName() {
        //given
        ERole nameTest = ERole.ROLE_FARM_MANAGER;

        //when
        Optional<Role> foundRole = roleRepository.findByName(nameTest);

        //then
        assertThat(foundRole.isPresent(), is(true));
        assertThat(foundRole.get(), notNullValue());
        assertThat(foundRole.get().getName(), is(nameTest));
    }

    @Test
    @DisplayName("Tests that non existing role does not exist")
    public void testDoesNotFindNonExistingRoleByName() {
        //when
        Optional<Role> foundRole = roleRepository.findByName(null);

        //then
        assertThat(foundRole.isPresent(), is(false));
    }
}
