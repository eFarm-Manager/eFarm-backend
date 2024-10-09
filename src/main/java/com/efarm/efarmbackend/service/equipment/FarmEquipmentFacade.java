package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.*;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.equipment.EquipmentCategoryRepository;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

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

    @Autowired
    private EquipmentCategoryRepository equipmentCategoryRepository;

    @Autowired
    private ValidationRequestService validationRequestService;

    private static final Logger logger = LoggerFactory.getLogger(FarmEquipmentFacade.class);

    public ResponseEntity<List<FarmEquipmentShortDTO>> getFarmEquipment(String searchQuery) {
        List<FarmEquipment> equipmentList = farmEquipmentRepository.findByFarmIdFarm_Id(userService.getLoggedUserFarm().getId());

        return ResponseEntity.ok(equipmentList.stream()
                .filter(equipment -> (searchQuery == null || searchQuery.isBlank() ||
                                equipment.getEquipmentName().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT)) ||
                                equipment.getBrand().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT)) ||
                                equipment.getCategory().getCategoryName().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT))
                        ) && (equipment.getIsAvailable())
                )
                .map(equipment -> new FarmEquipmentShortDTO(
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

            if (equipment.getIsAvailable()) {
                List<String> fieldsToDisplay = equipmentDisplayDataService.getFieldsForCategory(equipment.getCategory().getCategoryName());
                AddUpdateFarmEquipmentRequest equipmentDetailDTO = FarmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fieldsToDisplay);

                return ResponseEntity.ok(equipmentDetailDTO);
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Wybrany sprzęt już nie istnieje"));
            }
        } catch (RuntimeException e) {
            logger.info("Equipment with id: {}, not found for farm with id: {}",equipmentId, farmEquipmentId.getFarmId());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    public ResponseEntity<List<EquipmentCategoryDTO>> getEquipmentCategoriesWithFields() {
        return ResponseEntity.ok(equipmentDisplayDataService.getAllCategoriesWithFields());
    }

    @Transactional
    public ResponseEntity<?> addNewFarmEquipment(AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, BindingResult bindingResult) {

        ResponseEntity<?> validationErrorResponse = validationRequestService.validateRequest(bindingResult);
        if (validationErrorResponse != null) {
            return validationErrorResponse;
        }

        Farm loggedUserFarm = userService.getLoggedUserFarm();
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(farmEquipmentRepository.findNextFreeIdForFarm(loggedUserFarm.getId()), loggedUserFarm.getId());
        FarmEquipment equipment = new FarmEquipment(farmEquipmentId , equipmentCategoryRepository.findByCategoryName(addUpdateFarmEquipmentRequest.getCategory()), loggedUserFarm);

        if(farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(addUpdateFarmEquipmentRequest.getEquipmentName(), loggedUserFarm)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Maszyna o podanej nazwie już istnieje"));
        }

        farmEquipmentService.setCommonFieldsForCategory(addUpdateFarmEquipmentRequest, equipment);
        farmEquipmentService.setSpecificFieldsForCategory(addUpdateFarmEquipmentRequest, equipment, addUpdateFarmEquipmentRequest.getCategory());

        farmEquipmentRepository.save(equipment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Pomyślnie dodano nową maszynę"));
    }

    @Transactional
    public ResponseEntity<?> updateFarmEquipment(Integer equipmentId, AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest, BindingResult bindingResult) {

        ResponseEntity<?> validationErrorResponse = validationRequestService.validateRequest(bindingResult);
        if (validationErrorResponse != null) {
            return validationErrorResponse;
        }

        Farm loggedUserFarm = userService.getLoggedUserFarm();
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, loggedUserFarm.getId());

        try {
            FarmEquipment equipment = farmEquipmentRepository.findById(farmEquipmentId)
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono maszyny o id: " + equipmentId));

            if (equipment.getIsAvailable()) {
                if(farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(addUpdateFarmEquipmentRequest.getEquipmentName(), loggedUserFarm)) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Maszyna o podanej nazwie już występuje w gospodarstwie"));
                }
                farmEquipmentService.setCommonFieldsForCategory(addUpdateFarmEquipmentRequest, equipment);
                farmEquipmentService.setSpecificFieldsForCategory(addUpdateFarmEquipmentRequest, equipment, equipment.getCategory().getCategoryName());
                farmEquipmentRepository.save(equipment);
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Wybrany sprzęt już nie istnieje"));
            }
        } catch (RuntimeException e) {
            logger.info("Equipment with id: {}, can not by update for farm with id: {}",equipmentId, farmEquipmentId.getFarmId());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
        return ResponseEntity.ok(new MessageResponse("Pomyślnie zaktualizowane dane maszyny."));
    }

    public ResponseEntity<?> deleteFarmEquipment(Integer equipmentId) {

        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, userService.getLoggedUserFarm().getId());

        try {
            FarmEquipment equipment = farmEquipmentRepository.findById(farmEquipmentId)
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono maszyny o id: " + equipmentId));

            if (equipment.getIsAvailable()) {
                equipment.setIsAvailable(false);
                farmEquipmentRepository.save(equipment);
                return ResponseEntity.ok(new MessageResponse("Pomyślnie usunięto maszynę"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Wybrana maszyna została już usunięta"));
    }
}

