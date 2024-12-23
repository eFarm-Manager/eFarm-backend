package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface FarmEquipmentService {
    AddUpdateFarmEquipmentRequest createFarmEquipmentDTOtoDisplay(FarmEquipment equipment, List<String> fieldsToDisplay);

    void setSpecificFieldsForCategory(AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, FarmEquipment equipment, String categoryName);

    void setCommonFieldsForCategory(AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, FarmEquipment equipment);

    @Transactional
    void deleteAllEquipmentForFarm(Farm farm);

    List<FarmEquipment> getEquipmentByIds(List<Integer> equipmentIds, Farm userFarm);
}
