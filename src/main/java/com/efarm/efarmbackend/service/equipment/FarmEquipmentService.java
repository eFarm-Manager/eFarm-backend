package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface FarmEquipmentService {
    static AddUpdateFarmEquipmentRequest createFarmEquipmentDTOtoDisplay(FarmEquipment equipment, List<String> fieldsToDisplay) {
        AddUpdateFarmEquipmentRequest equipmentDetailDTO = new AddUpdateFarmEquipmentRequest(
                equipment.getId().getId(),
                equipment.getEquipmentName(),
                equipment.getCategory().getCategoryName(),
                equipment.getBrand(),
                equipment.getModel()
        );

        if (fieldsToDisplay.contains("power")) {
            equipmentDetailDTO.setPower(equipment.getPower());
        }
        if (fieldsToDisplay.contains("capacity")) {
            equipmentDetailDTO.setCapacity(equipment.getCapacity());
        }
        if (fieldsToDisplay.contains("workingWidth")) {
            equipmentDetailDTO.setWorkingWidth(equipment.getWorkingWidth());
        }
        if (fieldsToDisplay.contains("insurancePolicyNumber")) {
            equipmentDetailDTO.setInsurancePolicyNumber(equipment.getInsurancePolicyNumber());
        }
        if (fieldsToDisplay.contains("insuranceExpirationDate")) {
            equipmentDetailDTO.setInsuranceExpirationDate(equipment.getInsuranceExpirationDate());
        }
        if (fieldsToDisplay.contains("inspectionExpireDate")) {
            equipmentDetailDTO.setInspectionExpireDate(equipment.getInspectionExpireDate());
        }
        return equipmentDetailDTO;
    }

    void setSpecificFieldsForCategory(AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, FarmEquipment equipment, String categoryName);

    void setCommonFieldsForCategory(AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, FarmEquipment equipment);

    @Transactional
    void deleteAllEquipmentForFarm(Farm farm);

    List<FarmEquipment> getEquipmentByIds(List<Integer> equipmentIds, Farm userFarm);
}
