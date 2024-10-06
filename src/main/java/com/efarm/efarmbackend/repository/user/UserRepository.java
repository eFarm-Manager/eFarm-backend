package com.efarm.efarmbackend.repository.user;

import com.efarm.efarmbackend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    List<User> findByFarmId(Integer farmId);

    @Query("SELECT u FROM User u WHERE u.role.name = 'ROLE_FARM_OWNER' AND u.farm.id = :farmId")
    List<User> findOwnersForFarm(@Param("farmId") Integer farmId);
}
