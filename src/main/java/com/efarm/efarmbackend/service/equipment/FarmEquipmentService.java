package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentDTO;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.mysql.cj.conf.PropertyKey.logger;

@Service
public class FarmEquipmentService {

    @Autowired
    private EquipmentDisplayDataService equipmentDisplayDataService;

    private static final Logger logger = LoggerFactory.getLogger(FarmEquipmentService.class);
    @Autowired
    private FarmEquipmentRepository farmEquipmentRepository;

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

    public void setSpecificFieldsForCategory(FarmEquipmentDTO farmEquipmentDTO, FarmEquipment equipment, String categoryName) {
        List<String> fieldsForCategory = equipmentDisplayDataService.getFieldsForCategory(categoryName);
        if (fieldsForCategory.contains("power")) {
            if (farmEquipmentDTO.getPower() != null) {
                equipment.setPower(farmEquipmentDTO.getPower());
            }
        }
        if (fieldsForCategory.contains("capacity")) {
            if (farmEquipmentDTO.getCapacity() != null) {
                equipment.setCapacity(farmEquipmentDTO.getCapacity());
            }
        }
        if (fieldsForCategory.contains("workingWidth")) {
            if (farmEquipmentDTO.getWorkingWidth() != null) {
                equipment.setWorkingWidth(farmEquipmentDTO.getWorkingWidth());
            }
        }
        if (fieldsForCategory.contains("insurancePolicyNumber")) {
            if (farmEquipmentDTO.getInsurancePolicyNumber() != null) {
                equipment.setInsurancePolicyNumber(farmEquipmentDTO.getInsurancePolicyNumber());
            }
        }
        if (fieldsForCategory.contains("insuranceExpirationDate")) {
            if (farmEquipmentDTO.getInsuranceExpirationDate() != null) {
                equipment.setInsuranceExpirationDate(farmEquipmentDTO.getInsuranceExpirationDate());
            }
        }
        if (fieldsForCategory.contains("inspectionExpireDate")) {
            if (farmEquipmentDTO.getInspectionExpireDate() != null) {
                equipment.setInspectionExpireDate(farmEquipmentDTO.getInspectionExpireDate());
            }
        }
    }

    public void setCommonFieldsForCategory(FarmEquipmentDTO farmEquipmentDTO, FarmEquipment equipment) {
        if(farmEquipmentDTO.getEquipmentName() != null) {
            equipment.setEquipmentName(farmEquipmentDTO.getEquipmentName());
        }
        if(farmEquipmentDTO.getBrand() != null) {
            equipment.setBrand(farmEquipmentDTO.getBrand());
        }
        if(farmEquipmentDTO.getModel() != null) {
            equipment.setModel(farmEquipmentDTO.getModel());
        }

        Integer nowy = farmEquipmentRepository.findNextFreeId();
        logger.warn("Największe id: {}", nowy);
    }
}
