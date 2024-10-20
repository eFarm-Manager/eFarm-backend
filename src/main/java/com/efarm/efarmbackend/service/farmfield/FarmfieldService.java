package com.efarm.efarmbackend.service.farmfield;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farmfield.Farmfield;
import com.efarm.efarmbackend.model.farmfield.FarmfieldId;
import com.efarm.efarmbackend.repository.farmfield.FarmfieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FarmfieldService {

    @Autowired
    private FarmfieldRepository farmfieldRepository;

    public Farmfield createFarmfield(Farm loggedUserFarm, String farmfieldName, Double area) {
        FarmfieldId farmfieldId = new FarmfieldId(farmfieldRepository.findNextFreeIdForFarm(loggedUserFarm.getId()), loggedUserFarm.getId());
        Farmfield farmfield = new Farmfield(farmfieldId, loggedUserFarm, farmfieldName, area);
        farmfieldRepository.save(farmfield);
        return farmfield;
    }
}
