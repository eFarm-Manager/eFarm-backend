package com.efarm.efarmbackend.repository.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Integer> {

    Season findByName(String name);
}