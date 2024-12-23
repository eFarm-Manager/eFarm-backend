package com.efarm.efarmbackend.repository.farm;

import com.efarm.efarmbackend.model.farm.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
}