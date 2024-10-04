package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmEquipmentService {

    public static FarmEquipmentDTO createFarmEquipmentDTOtoDisplay(FarmEquipment equipment, List<String> fieldsToDisplay) {
        FarmEquipmentDTO equipmentDetailDTO = new FarmEquipmentDTO(
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
}
