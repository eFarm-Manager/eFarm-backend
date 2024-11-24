package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agroactivity.ActivityCategory;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId;
import com.efarm.efarmbackend.model.agroactivity.AgroActivitySummaryDTO;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.agroactivity.NewAgroActivityRequest;
import com.efarm.efarmbackend.payload.request.agroactivity.UpdateAgroActivityRequest;
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository;
import com.efarm.efarmbackend.repository.agroactivity.ActivityHasEquipmentRepository;
import com.efarm.efarmbackend.repository.agroactivity.ActivityHasOperatorRepository;
import com.efarm.efarmbackend.repository.agroactivity.AgroActivityRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AgroActivityService {

    private final ActivityHasOperatorRepository activityHasOperatorRepository;
    private final UserService userService;
    private final ActivityHasEquipmentRepository activityHasEquipmentRepository;
    private final AgroActivityRepository agroActivityRepository;
    private final AgriculturalRecordRepository agriculturalRecordRepository;

    public AgroActivity createNewAgroActivity(NewAgroActivityRequest request, ActivityCategory activityCategory, AgriculturalRecord agriculturalRecord, Integer loggedUserFarmId) {
        AgroActivityId agroActivityId = new AgroActivityId(
                agroActivityRepository.findNextFreeIdForFarm(loggedUserFarmId),
                loggedUserFarmId
        );
        AgroActivity agroActivity = new AgroActivity(agroActivityId, activityCategory, agriculturalRecord, request);
        agroActivity.setDate(Optional.ofNullable(request.getDate()).orElse(Instant.now()));
        agroActivity.setIsCompleted(Optional.ofNullable(request.getIsCompleted()).orElse(true));
        agroActivityRepository.save(agroActivity);
        return agroActivity;
    }

    public List<AgroActivitySummaryDTO> getAgroActivitiesByAgriculturalRecord(Integer id) {
        Integer farmId = userService.getLoggedUserFarm().getId();
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(id, farmId);
        AgriculturalRecord agriculturalRecord = agriculturalRecordRepository.findById(agriculturalRecordId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono ewidencji"));

        List<AgroActivity> activities = agroActivityRepository.findByAgriculturalRecord(agriculturalRecord);
        return activities.stream()
                .map(AgroActivitySummaryDTO::new)
                .collect(Collectors.toList());
    }

    public AgroActivity findAgroActivityWithDetails(Integer id, Integer loggedUserFarmId) {
        AgroActivityId agroActivityId = new AgroActivityId(id, loggedUserFarmId);
        return agroActivityRepository.findWithDetailsById(agroActivityId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono zabiegu agrotechnicznego"));
    }

    public void updateAgroActivity(UpdateAgroActivityRequest request, AgroActivity agroActivity, ActivityCategory activityCategory) {
        if (request.getName() != null && !request.getName().isEmpty()) {
            agroActivity.setName(request.getName());
        }
        if (request.getDate() != null) {
            agroActivity.setDate(request.getDate());
        }
        if (request.getIsCompleted() != null) {
            agroActivity.setIsCompleted(request.getIsCompleted());
        }
        if (activityCategory != null) {
            agroActivity.setActivityCategory(activityCategory);
        }
        if (request.getUsedSubstances() != null) {
            agroActivity.setUsedSubstances(request.getUsedSubstances());
        }
        if (request.getAppliedDose() != null) {
            agroActivity.setAppliedDose(request.getAppliedDose());
        }
        if (request.getDescription() != null) {
            agroActivity.setDescription(request.getDescription());
        }
        agroActivityRepository.save(agroActivity);
    }

    public void deleteAgroActivity(AgroActivityId agroActivityId) {
        AgroActivity agroActivity = agroActivityRepository.findById(agroActivityId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zabiegu agrotechnicznego o ID: " + agroActivityId.getId()));

        activityHasOperatorRepository.deleteActivityHasOperatorsByAgroActivity(agroActivity);
        activityHasEquipmentRepository.deleteActivityHasEquipmentsByAgroActivity(agroActivity);
        agroActivityRepository.delete(agroActivity);
    }

    @Transactional
    public List<AgroActivitySummaryDTO> getAssignedIncompleteActivitiesForLoggedUser() {
        User loggedUser = userService.getLoggedUser();
        List<AgroActivity> activities = agroActivityRepository.findIncompleteActivitiesAssignedToUser(loggedUser.getId());
        return activities.stream()
                .sorted(Comparator.comparing(AgroActivity::getDate))
                .map(AgroActivitySummaryDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markActivityAsCompleted(Integer activityId) {
        AgroActivity activity = agroActivityRepository.findById(new AgroActivityId(activityId, userService.getLoggedUserFarm().getId()))
                .orElseThrow(() -> new RuntimeException("Nie znaleziono zadania"));

        User loggedUser = userService.getLoggedUser();
        boolean isAssigned = activityHasOperatorRepository.findActivityHasOperatorsByAgroActivity(activity).stream()
                .anyMatch(operator -> operator.getUser().getId().equals(loggedUser.getId()));

        if (!isAssigned || activity.getIsCompleted()) {
            throw new RuntimeException("Nie masz dostępu do tego zadania");
        }

        activity.setIsCompleted(true);
        agroActivityRepository.save(activity);
    }
}