package com.efarm.efarmbackend.service.landparcel;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import jakarta.transaction.Transactional;

public interface LandparcelService {
    void addNewLandparcelData(LandparcelDTO landparcelDTO, Landparcel landparcel);

    void updateLandparcelData(LandparcelDTO landparcelDTO, Landparcel landparcel);

    Boolean isLandparcelAlreadyExistingByFarm(LandparcelDTO landparcelDTO, Farm loggedUserFarm);

    Boolean isLandparcelNameTaken(String name, Farm loggedUserFarm);

    Landparcel findlandparcelByFarm(Integer id, Farm loggedUserFarm) throws Exception;

    @Transactional
    void deleteAllLandparcelsForFarm(Farm farm);

    void setCommonFields(Landparcel landparcel, LandparcelDTO landparcelDTO);

    void setAdministrativeData(Landparcel landparcel, LandparcelDTO landparcelDTO);
}
