package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentShortDTO;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.equipment.EquipmentCategoryRepository;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private EquipmentCategoryRepository equipmentCategoryRepository;

    private static final Logger logger = LoggerFactory.getLogger(FarmEquipmentFacade.class);

    public List<FarmEquipmentShortDTO> getFarmEquipment(String searchQuery) {
        List<FarmEquipment> equipmentList = farmEquipmentRepository.findByFarmIdFarm_Id(userService.getLoggedUserFarm().getId());

        return equipmentList.stream()
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
                .collect(Collectors.toList());
    }

    public AddUpdateFarmEquipmentRequest getEquipmentDetails(Integer equipmentId) throws RuntimeException {
        Farm currentUserFarm = userService.getLoggedUserFarm();
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, currentUserFarm.getId());
        FarmEquipment equipment = farmEquipmentRepository.findById(farmEquipmentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono maszyny o id: " + farmEquipmentId.getId()));

        if (equipment.getIsAvailable()) {
            List<String> fieldsToDisplay = equipmentDisplayDataService.getFieldsForCategory(equipment.getCategory().getCategoryName());

            return FarmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fieldsToDisplay);
        } else {
            throw new RuntimeException("Sprzęt jest niedostępny!");
        }
    }

    public List<EquipmentCategoryDTO> getEquipmentCategoriesWithFields() {
        return equipmentDisplayDataService.getAllCategoriesWithFields();
    }

    @Transactional
    public MessageResponse addNewFarmEquipment(AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest) throws RuntimeException {

        Farm loggedUserFarm = userService.getLoggedUserFarm();
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(farmEquipmentRepository.findNextFreeIdForFarm(loggedUserFarm.getId()), loggedUserFarm.getId());
        FarmEquipment equipment = new FarmEquipment(farmEquipmentId, equipmentCategoryRepository.findByCategoryName(addUpdateFarmEquipmentRequest.getCategory()), loggedUserFarm);

        if (farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(addUpdateFarmEquipmentRequest.getEquipmentName(), loggedUserFarm)) {
            throw new RuntimeException("Maszyna o podanej nazwie już istnieje");
        }
        farmEquipmentService.setCommonFieldsForCategory(addUpdateFarmEquipmentRequest, equipment);
        farmEquipmentService.setSpecificFieldsForCategory(addUpdateFarmEquipmentRequest, equipment, addUpdateFarmEquipmentRequest.getCategory());
        farmEquipmentRepository.save(equipment);
        return new MessageResponse("Pomyślnie dodano nową maszynę");
    }

    @Transactional
    public MessageResponse updateFarmEquipment(Integer equipmentId, AddUpdateFarmEquipmentRequest addUpdateFarmEquipmentRequest) throws RuntimeException {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, loggedUserFarm.getId());

        FarmEquipment equipment = farmEquipmentRepository.findById(farmEquipmentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono maszyny o id: " + equipmentId));

        if (equipment.getIsAvailable()) {
            if (farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(addUpdateFarmEquipmentRequest.getEquipmentName(), loggedUserFarm)) {
                throw new RuntimeException("Maszyna o podanej nazwie już występuje w gospodarstwie");
            }
            farmEquipmentService.setCommonFieldsForCategory(addUpdateFarmEquipmentRequest, equipment);
            farmEquipmentService.setSpecificFieldsForCategory(addUpdateFarmEquipmentRequest, equipment, equipment.getCategory().getCategoryName());
            farmEquipmentRepository.save(equipment);
        } else {
            throw new RuntimeException("Wybrany sprzęt już nie istnieje");
        }
        return new MessageResponse("Pomyślnie zaktualizowane dane maszyny.");
    }

    public MessageResponse deleteFarmEquipment(Integer equipmentId) {

        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, userService.getLoggedUserFarm().getId());
        FarmEquipment equipment = farmEquipmentRepository.findById(farmEquipmentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono maszyny o id: " + equipmentId));

        if (equipment.getIsAvailable()) {
            equipment.setIsAvailable(false);
            farmEquipmentRepository.save(equipment);
            return new MessageResponse("Pomyślnie usunięto maszynę");
        } else {
            throw new RuntimeException("Wybrana maszyna została już usunięta");
        }
    }
}

