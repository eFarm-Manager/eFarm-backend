package com.efarm.efarmbackend.repository.farm;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivationCodeRepository extends JpaRepository<ActivationCode, Integer> {

    Optional<ActivationCode> findByCode(String code);

    ActivationCode findById(int id);
}