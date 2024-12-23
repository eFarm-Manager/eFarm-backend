package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.user.UserSummaryDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ActivityHasOperatorService {
    @Transactional
    void addOperatorsToActivity(AgroActivity agroActivity, List<Integer> operatorIds, Integer loggedUserFarmId);

    List<UserSummaryDTO> getOperatorsForAgroActivity(AgroActivity agroActivity);

    void updateOperatorInActivity(List<Integer> operatorsIds, AgroActivity agroActivity, Integer loggedUserFarmId);
}
