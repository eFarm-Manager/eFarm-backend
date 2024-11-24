package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FarmEquipmentService {

    private final EquipmentDisplayDataService equipmentDisplayDataService;
    private final FarmEquipmentRepository farmEquipmentRepository;

    public static AddUpdateFarmEquipmentRequest createFarmEquipmentDTOtoDisplay(FarmEquipment equipment, List<String> fieldsToDisplay) {
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

    public void setSpecificFieldsForCategory(AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, FarmEquipment equipment, String categoryName) {
        List<String> fieldsForCategory = equipmentDisplayDataService.getFieldsForCategory(categoryName);
        if (fieldsForCategory.contains("power")) {
            if (addUpdateFarmEquipmentRequest.getPower() != null) {
                equipment.setPower(addUpdateFarmEquipmentRequest.getPower());
            }
        }
        if (fieldsForCategory.contains("capacity")) {
            if (addUpdateFarmEquipmentRequest.getCapacity() != null) {
                equipment.setCapacity(addUpdateFarmEquipmentRequest.getCapacity());
            }
        }
        if (fieldsForCategory.contains("workingWidth")) {
            if (addUpdateFarmEquipmentRequest.getWorkingWidth() != null) {
                equipment.setWorkingWidth(addUpdateFarmEquipmentRequest.getWorkingWidth());
            }
        }
        if (fieldsForCategory.contains("insurancePolicyNumber")) {
            if (addUpdateFarmEquipmentRequest.getInsurancePolicyNumber() != null) {
                equipment.setInsurancePolicyNumber(addUpdateFarmEquipmentRequest.getInsurancePolicyNumber());
            }
        }
        if (fieldsForCategory.contains("insuranceExpirationDate")) {
            if (addUpdateFarmEquipmentRequest.getInsuranceExpirationDate() != null) {
                equipment.setInsuranceExpirationDate(addUpdateFarmEquipmentRequest.getInsuranceExpirationDate());
            }
        }
        if (fieldsForCategory.contains("inspectionExpireDate")) {
            if (addUpdateFarmEquipmentRequest.getInspectionExpireDate() != null) {
                equipment.setInspectionExpireDate(addUpdateFarmEquipmentRequest.getInspectionExpireDate());
            }
        }
    }

    public void setCommonFieldsForCategory(AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, FarmEquipment equipment) {
        if (addUpdateFarmEquipmentRequest.getEquipmentName() != null) {
            equipment.setEquipmentName(addUpdateFarmEquipmentRequest.getEquipmentName());
        }
        if (addUpdateFarmEquipmentRequest.getBrand() != null) {
            equipment.setBrand(addUpdateFarmEquipmentRequest.getBrand());
        }
        if (addUpdateFarmEquipmentRequest.getModel() != null) {
            equipment.setModel(addUpdateFarmEquipmentRequest.getModel());
        }
    }

    @Transactional
    public void deleteAllEquipmentForFarm(Farm farm) {
        List<FarmEquipment> equipments = farmEquipmentRepository.findByFarmIdFarm_Id(farm.getId());
        farmEquipmentRepository.deleteAll(equipments);
    }

    public List<FarmEquipment> getEquipmentByIds(List<Integer> equipmentIds, Farm userFarm) {

        List<FarmEquipmentId> equipmentKeys = equipmentIds.stream()
                .map(equipmentId -> new FarmEquipmentId(equipmentId, userFarm.getId()))
                .collect(Collectors.toList());
        List<FarmEquipment> foundEquipment = farmEquipmentRepository.findAllById(equipmentKeys);

        if (foundEquipment.size() != equipmentIds.size()) {
            List<Integer> missingIds = equipmentIds.stream()
                    .filter(id -> foundEquipment.stream()
                            .noneMatch(equipment -> equipment.getId().getId().equals(id)))
                    .toList();
            throw new RuntimeException("Nie znaleziono maszyn o następujących ID: " + missingIds);
        }

        List<FarmEquipment> inactiveEquipment = foundEquipment.stream()
                .filter(equipment -> !equipment.getIsAvailable())
                .toList();
        if (!inactiveEquipment.isEmpty()) {
            String inactiveNames = inactiveEquipment.stream()
                    .map(FarmEquipment::getEquipmentName)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Niektóre maszyny są nieaktywne: " + inactiveNames);
        }
        return foundEquipment;
    }
}