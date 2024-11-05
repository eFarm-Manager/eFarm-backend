package com.efarm.efarmbackend.repository.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.ActivityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityCategoryRepository extends JpaRepository<ActivityCategory, Integer> {
    Optional<ActivityCategory> findByName(String name);
}
