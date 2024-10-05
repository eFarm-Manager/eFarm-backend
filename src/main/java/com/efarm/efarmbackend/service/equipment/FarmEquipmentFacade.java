package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import com.efarm.efarmbackend.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class FarmEquipmentFacade {

    @Autowired
    private FarmEquipmentRepository farmEquipmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EquipmentDisplayDataService equipmentDisplayDataService;

    @Autowired
    FarmEquipmentService farmEquipmentService;

    private static final Logger logger = LoggerFactory.getLogger(FarmEquipmentFacade.class);

    public ResponseEntity<List<FarmEquipmentDTO>> getFarmEquipment(String searchQuery) {
        List<FarmEquipment> equipmentList = farmEquipmentRepository.findByFarmIdFarm_Id(userService.getLoggedUserFarm().getId());

        return ResponseEntity.ok(equipmentList.stream()
                .filter(equipment -> (searchQuery == null || searchQuery.isBlank() ||
                                equipment.getEquipmentName().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT)) ||
                                equipment.getBrand().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT)) ||
                                equipment.getCategory().getCategoryName().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT))
                        ) && (equipment.getIsAvailable())
                )
                .map(equipment -> new FarmEquipmentDTO(
                        equipment.getId().getId(),
                        equipment.getEquipmentName(),
                        equipment.getCategory().getCategoryName(),
                        equipment.getBrand(),
                        equipment.getModel()
                ))
                .collect(Collectors.toList()));
    }

    public ResponseEntity<?> getEquipmentDetails(Integer equipmentId) {
        Farm currentUserFarm = userService.getLoggedUserFarm();
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, currentUserFarm.getId());
        try {
            FarmEquipment equipment = farmEquipmentRepository.findById(farmEquipmentId)
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono maszyny o id: " + farmEquipmentId.getId()));

            List<String> fieldsToDisplay = equipmentDisplayDataService.getFieldsForCategory(equipment.getCategory().getCategoryName());
            FarmEquipmentDTO equipmentDetailDTO = FarmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fieldsToDisplay);

            return ResponseEntity.ok(equipmentDetailDTO);
        } catch (RuntimeException e) {
            logger.info("Equipment with id: {}, not found for farm with id: {}",equipmentId, farmEquipmentId.getFarmId());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    public ResponseEntity<List<EquipmentCategoryDTO>> getEquipmentCategoriesWithFields() {
        return ResponseEntity.ok(equipmentDisplayDataService.getAllCategoriesWithFields());
    }
}

