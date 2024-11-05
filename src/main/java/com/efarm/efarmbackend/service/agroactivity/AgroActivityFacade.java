package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agroactivity.ActivityCategory;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityDetailDTO;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentShortDTO;
import com.efarm.efarmbackend.model.landparcel.LandparcelSummaryDTO;
import com.efarm.efarmbackend.model.user.UserSummaryDTO;
import com.efarm.efarmbackend.payload.request.agroactivity.NewAgroActivityRequest;
import com.efarm.efarmbackend.payload.request.agroactivity.UpdateAgroActivityRequest;
import com.efarm.efarmbackend.repository.agroactivity.ActivityCategoryRepository;
import com.efarm.efarmbackend.service.agriculturalrecords.AgriculturalRecordService;
import com.efarm.efarmbackend.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgroActivityFacade {

    @Autowired
    private ActivityCategoryRepository activityCategoryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AgroActivityService agroActivityService;

    @Autowired
    private AgriculturalRecordService agriculturalRecordService;

    @Autowired
    private ActivityHasEquipmentService activityHasEquipmentService;

    @Autowired
    private ActivityHasOperatorService activityHasOperatorService;

    @Transactional
    public void addAgroActivity(NewAgroActivityRequest request) {

        Integer loggedUserFarmId = userService.getLoggedUserFarm().getId();
        ActivityCategory activityCategory = activityCategoryRepository.findByName(request.getActivityCategoryName())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono kategorii zabiegu"));

        AgriculturalRecord agriculturalRecord = agriculturalRecordService.findAgriculturalRecordById(request.getAgriculturalRecordId(), loggedUserFarmId);
        AgroActivity agroActivity = agroActivityService.createNewAgroActivity(request, activityCategory, agriculturalRecord, loggedUserFarmId);

        activityHasOperatorService.addOperatorsToActivity(agroActivity, request.getOperatorIds(), loggedUserFarmId);
        activityHasEquipmentService.addEquipmentToActivity(request.getEquipmentIds(), agroActivity, loggedUserFarmId);
    }


    public AgroActivityDetailDTO getAgroActivityDetails(Integer id) {

        Integer loggedUserFarmId = userService.getLoggedUserFarm().getId();
        AgroActivity agroActivity = agroActivityService.findAgroActivityWithDetails(id, loggedUserFarmId);

        LandparcelSummaryDTO landparcelSummaryDTO = new LandparcelSummaryDTO(agroActivity);
        List<UserSummaryDTO> operators = activityHasOperatorService.getOperatorsForAgroActivity(agroActivity);
        List<FarmEquipmentShortDTO> equipments = activityHasEquipmentService.getEquipmentsForAgroActivity(agroActivity);

        return new AgroActivityDetailDTO(agroActivity, landparcelSummaryDTO, operators, equipments);
    }

    @Transactional
    public void updateAgroActivity(Integer agroActivityId, UpdateAgroActivityRequest request) {
        Integer loggedUserFarmId = userService.getLoggedUserFarm().getId();

        AgroActivity agroActivity = agroActivityService.findAgroActivityWithDetails(agroActivityId, loggedUserFarmId);
        ActivityCategory activityCategory = activityCategoryRepository.findByName(request.getActivityCategoryName())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono kategorii zabiegu"));

        agroActivityService.updateAgroActivity(request, agroActivity, activityCategory);
        activityHasOperatorService.updateOperatorInActivity(request.getOperatorIds(), agroActivity, loggedUserFarmId);
        activityHasEquipmentService.updateEqipmentInActivity(request.getEquipmentIds(), agroActivity, loggedUserFarmId);
    }

    @Transactional
    public void deleteAgroActivity(Integer id) {
        Integer loggedUserFarmId = userService.getLoggedUserFarm().getId();
        AgroActivityId agroActivityId = new AgroActivityId(id, loggedUserFarmId);
        agroActivityService.deleteAgroActivity(agroActivityId);
    }
}