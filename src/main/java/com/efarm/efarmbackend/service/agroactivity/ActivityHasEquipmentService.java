package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.ActivityHasEquipment;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.repository.agroactivity.ActivityHasEquipmentRepository;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityHasEquipmentService {

    @Autowired
    private FarmEquipmentRepository farmEquipmentRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ActivityHasEquipmentRepository activityHasEquipmentRepository;

    @Transactional
    public void addEquipmentToActivity(List<Integer> equipmentIds, AgroActivity agroActivity, Integer loggedUserFarmId) {
        if (equipmentIds != null && !equipmentIds.isEmpty()) {
            List<FarmEquipmentId> farmEquipmentIds = equipmentIds.stream()
                    .map(id -> new FarmEquipmentId(id, loggedUserFarmId))
                    .collect(Collectors.toList());

            List<FarmEquipmentId> existingEquipmentIds = farmEquipmentRepository.findAllById(farmEquipmentIds)
                    .stream()
                    .map(FarmEquipment::getId)
                    .toList();

            List<Integer> missingEquipmentIds = farmEquipmentIds.stream()
                    .filter(id -> !existingEquipmentIds.contains(id))
                    .map(FarmEquipmentId::getId)
                    .toList();

            if (!missingEquipmentIds.isEmpty()) {
                throw new IllegalArgumentException("Sprzęty o następujących identyfikatorach nie istnieją: " + missingEquipmentIds);
            }

            List<FarmEquipment> equipments = farmEquipmentRepository.findAllById(farmEquipmentIds);

            for (FarmEquipment equipment : equipments) {
                if (equipment.getIsAvailable()) {
                    ActivityHasEquipment activityHasEquipment = new ActivityHasEquipment();
                    activityHasEquipment.setAgroActivity(agroActivity);
                    activityHasEquipment.setFarmEquipment(equipment);
                    activityHasEquipment.setFarmId(loggedUserFarmId);
                    activityHasEquipmentRepository.save(activityHasEquipment);
                } else {
                    throw new IllegalStateException("Sprzęt " + equipment.getEquipmentName() + " jest niedostępny");
                }
            }
        }
    }

    public List<EquipmentSummaryDTO> getEquipmentsForAgroActivity(AgroActivity agroActivity) {
        return activityHasEquipmentRepository.findActivityHasEquipmentsByAgroActivity(agroActivity).stream()
                .map(EquipmentSummaryDTO::new)
                .collect(Collectors.toList());
    }

    public void updateEquipmentInActivity(List<Integer> equipmentIds, AgroActivity agroActivity, Integer loggedUserFarmId) {
        activityHasEquipmentRepository.deleteActivityHasEquipmentsByAgroActivity(agroActivity);
        ActivityHasEquipmentService self = applicationContext.getBean(ActivityHasEquipmentService.class);
        self.addEquipmentToActivity(equipmentIds, agroActivity, loggedUserFarmId);
    }
}