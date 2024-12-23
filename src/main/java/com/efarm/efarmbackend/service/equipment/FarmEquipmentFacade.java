package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.request.equipment.AddUpdateFarmEquipmentRequest;
import com.efarm.efarmbackend.repository.equipment.EquipmentCategoryRepository;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import com.efarm.efarmbackend.service.user.UserAuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FarmEquipmentFacade {

    private final FarmEquipmentRepository farmEquipmentRepository;
    private final UserAuthenticationService userAuthenticationService;
    private final EquipmentDisplayDataService equipmentDisplayDataService;
    private final FarmEquipmentService farmEquipmentService;
    private final EquipmentCategoryRepository equipmentCategoryRepository;

    public List<EquipmentSummaryDTO> getFarmEquipment(String searchQuery) {
        List<FarmEquipment> equipmentList = farmEquipmentRepository.findByFarmIdFarm_Id(userAuthenticationService.getLoggedUserFarm().getId());

        return equipmentList.stream()
                .filter(equipment -> (searchQuery == null || searchQuery.isBlank() ||
                                equipment.getEquipmentName().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT)) ||
                                equipment.getBrand().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT)) ||
                                equipment.getCategory().getCategoryName().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT))
                        ) && (equipment.getIsAvailable())
                )
                .map(EquipmentSummaryDTO::new)
                .collect(Collectors.toList());
    }

    public AddUpdateFarmEquipmentRequest getEquipmentDetails(Integer equipmentId) throws RuntimeException {
        Farm currentUserFarm = userAuthenticationService.getLoggedUserFarm();
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, currentUserFarm.getId());
        FarmEquipment equipment = farmEquipmentRepository.findById(farmEquipmentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono maszyny o id: " + farmEquipmentId.getId()));

        if (equipment.getIsAvailable()) {
            List<String> fieldsToDisplay = equipmentDisplayDataService.getFieldsForCategory(equipment.getCategory().getCategoryName());
            return farmEquipmentService.createFarmEquipmentDTOtoDisplay(equipment, fieldsToDisplay);
        } else {
            throw new RuntimeException("Sprzęt jest niedostępny");
        }
    }

    public List<EquipmentCategoryDTO> getEquipmentCategoriesWithFields() {
        return equipmentDisplayDataService.getAllCategoriesWithFields();
    }

    @Transactional
    public void addNewFarmEquipment(AddUpdateFarmEquipmentRequest request) throws RuntimeException {
        Farm loggedUserFarm = userAuthenticationService.getLoggedUserFarm();
        if (farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(
                request.getEquipmentName(),
                loggedUserFarm)) {
            throw new RuntimeException("Maszyna o podanej nazwie już istnieje");
        }
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(
                farmEquipmentRepository.findNextFreeIdForFarm(loggedUserFarm.getId()),
                loggedUserFarm.getId()
        );
        FarmEquipment equipment = new FarmEquipment(
                farmEquipmentId,
                equipmentCategoryRepository.findByCategoryName(request.getCategory()),
                loggedUserFarm
        );
        farmEquipmentService.setCommonFieldsForCategory(request, equipment);
        farmEquipmentService.setSpecificFieldsForCategory(
                request,
                equipment,
                request.getCategory()
        );
        farmEquipmentRepository.save(equipment);
    }

    @Transactional
    public void updateFarmEquipment(Integer equipmentId, AddUpdateFarmEquipmentRequest request) throws RuntimeException {
        Farm loggedUserFarm = userAuthenticationService.getLoggedUserFarm();
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(
                equipmentId,
                loggedUserFarm.getId()
        );

        FarmEquipment equipment = farmEquipmentRepository.findById(farmEquipmentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono maszyny o id: " + equipmentId));

        if (equipment.getIsAvailable()) {
            if (!equipment.getEquipmentName().equals(request.getEquipmentName()) &&
                    farmEquipmentRepository.existsByEquipmentNameAndFarmIdFarm(request.getEquipmentName(), loggedUserFarm)) {
                throw new RuntimeException("Maszyna o podanej nazwie już występuje w gospodarstwie");
            }
            farmEquipmentService.setCommonFieldsForCategory(request, equipment);
            farmEquipmentService.setSpecificFieldsForCategory(request, equipment, equipment.getCategory().getCategoryName());
            farmEquipmentRepository.save(equipment);
        } else {
            throw new RuntimeException("Wybrany sprzęt już nie istnieje");
        }
    }

    public void deleteFarmEquipment(Integer equipmentId) {
        FarmEquipmentId farmEquipmentId = new FarmEquipmentId(equipmentId, userAuthenticationService.getLoggedUserFarm().getId());
        FarmEquipment equipment = farmEquipmentRepository.findById(farmEquipmentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono maszyny o id: " + equipmentId));

        if (equipment.getIsAvailable()) {
            equipment.setIsAvailable(false);
            farmEquipmentRepository.save(equipment);
        } else {
            throw new RuntimeException("Wybrana maszyna została już usunięta");
        }
    }
}

