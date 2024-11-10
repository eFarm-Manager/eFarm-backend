package com.efarm.efarmbackend.repository.landparcel;

import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus;
import com.efarm.efarmbackend.model.landparcel.LandOwnershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LandOwnershipStatusRepository extends JpaRepository<LandOwnershipStatus, Integer> {

    Optional<LandOwnershipStatus> findByOwnershipStatus(ELandOwnershipStatus ownershipStatusEnum);
}