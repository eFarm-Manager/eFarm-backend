package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ActivityHasEquipmentService {
    @Transactional
    void addEquipmentToActivity(List<Integer> equipmentIds, AgroActivity agroActivity, Integer loggedUserFarmId);

    List<EquipmentSummaryDTO> getEquipmentsForAgroActivity(AgroActivity agroActivity);

    void updateEquipmentInActivity(List<Integer> equipmentIds, AgroActivity agroActivity, Integer loggedUserFarmId);
}
