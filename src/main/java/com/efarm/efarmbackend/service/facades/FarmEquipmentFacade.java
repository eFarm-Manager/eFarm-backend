package com.efarm.efarmbackend.service.facades;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentDTO;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import com.efarm.efarmbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FarmEquipmentFacade {

    @Autowired
    private FarmEquipmentRepository farmEquipmentRepository;

    @Autowired
    private UserService userService;

    public List<FarmEquipmentDTO> getFarmEquipment(String searchQuery, Boolean onlyAvailable) {
        boolean filterOnlyAvailable = (onlyAvailable != null && onlyAvailable);
        List<FarmEquipment> equipmentList = farmEquipmentRepository.findByFarmIdFarm_Id(userService.getLoggedUserFarm().getId());

        return equipmentList.stream()
                .filter(equipment ->
                        (searchQuery == null || searchQuery.isBlank() ||
                                equipment.getEquipmentName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                equipment.getBrand().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                equipment.getCategory().getCategoryName().toLowerCase().contains(searchQuery.toLowerCase())
                        ) &&
                                (!filterOnlyAvailable || equipment.getIsAvailable())
                )
                .map(equipment -> new FarmEquipmentDTO(
                        equipment.getEquipmentName(),
                        equipment.getCategory().getCategoryName(),
                        equipment.getIsAvailable(),
                        equipment.getBrand(),
                        equipment.getPower(),
                        equipment.getCapacity(),
                        equipment.getWorkingWidth(),
                        equipment.getInsurancePolicyNumber(),
                        equipment.getInsuranceExpirationDate(),
                        equipment.getInspectionExpireDate()))
                .collect(Collectors.toList());
    }
}
