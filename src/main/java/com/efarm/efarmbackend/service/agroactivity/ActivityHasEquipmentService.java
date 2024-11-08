package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.ActivityHasEquipment;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentShortDTO;
import com.efarm.efarmbackend.repository.agroactivity.ActivityHasEquipmentRepository;
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityHasEquipmentService {

    @Autowired
    private FarmEquipmentRepository farmEquipmentRepository;

    @Autowired
    private ActivityHasEquipmentRepository activityHasEquipmentRepository;

    public void addEquipmentToActivity(List<Integer> equipmentIds, AgroActivity agroActivity, Integer loggedUserFarmId) {
        if (equipmentIds != null && !equipmentIds.isEmpty()) {
            List<FarmEquipmentId> farmEquipmentIds = equipmentIds.stream()
                    .map(id -> new FarmEquipmentId(id, loggedUserFarmId))
                    .collect(Collectors.toList());

            List<FarmEquipment> equipments = farmEquipmentRepository.findAllById(farmEquipmentIds);

            for (FarmEquipment equipment : equipments) {
                if (equipment.getIsAvailable()) {
                    ActivityHasEquipment activityHasEquipment = new ActivityHasEquipment();
                    activityHasEquipment.setAgroActivity(agroActivity);
                    activityHasEquipment.setFarmEquipment(equipment);
                    activityHasEquipment.setFarmId(loggedUserFarmId);
                    activityHasEquipmentRepository.save(activityHasEquipment);
                } else {
                    throw new IllegalStateException("Sprzęt " + equipment.getEquipmentName() + " jest niedostępny!");
                }
            }
        }
    }

    public List<FarmEquipmentShortDTO> getEquipmentsForAgroActivity(AgroActivity agroActivity) {
        return activityHasEquipmentRepository.findActivityHasEquipmentsByAgroActivity(agroActivity).stream()
                .map(ahe -> new FarmEquipmentShortDTO(
                        ahe.getFarmEquipment().getId().getId(),
                        ahe.getFarmEquipment().getEquipmentName(),
                        ahe.getFarmEquipment().getCategory().getCategoryName(),
                        ahe.getFarmEquipment().getBrand(),
                        ahe.getFarmEquipment().getModel()
                ))
                .collect(Collectors.toList());
    }

    public void updateEqipmentInActivity(List<Integer> equipmentIds, AgroActivity agroActivity, Integer loggedUserFarmId) {
        activityHasEquipmentRepository.deleteActivityHasEquipmentsByAgroActivity(agroActivity);
        addEquipmentToActivity(equipmentIds, agroActivity, loggedUserFarmId);
    }
}
