package com.efarm.efarmbackend.repository.user;

import com.efarm.efarmbackend.domain.user.ERole;
import com.efarm.efarmbackend.domain.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
