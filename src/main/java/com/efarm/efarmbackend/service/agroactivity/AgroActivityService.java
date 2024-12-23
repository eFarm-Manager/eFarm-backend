package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agroactivity.ActivityCategory;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId;
import com.efarm.efarmbackend.model.agroactivity.AgroActivitySummaryDTO;
import com.efarm.efarmbackend.payload.request.agroactivity.NewAgroActivityRequest;
import com.efarm.efarmbackend.payload.request.agroactivity.UpdateAgroActivityRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface AgroActivityService {
    AgroActivity createNewAgroActivity(NewAgroActivityRequest request, ActivityCategory activityCategory, AgriculturalRecord agriculturalRecord, Integer loggedUserFarmId);

    List<AgroActivitySummaryDTO> getAgroActivitiesByAgriculturalRecord(Integer id);

    AgroActivity findAgroActivityWithDetails(Integer id, Integer loggedUserFarmId);

    void updateAgroActivity(UpdateAgroActivityRequest request, AgroActivity agroActivity, ActivityCategory activityCategory);

    void deleteAgroActivity(AgroActivityId agroActivityId);

    @Transactional
    List<AgroActivitySummaryDTO> getAssignedIncompleteActivitiesForLoggedUser();

    @Transactional
    void markActivityAsCompleted(Integer activityId);
}
