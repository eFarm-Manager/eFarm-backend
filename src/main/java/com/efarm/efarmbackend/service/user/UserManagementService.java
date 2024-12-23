package com.efarm.efarmbackend.service.user;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.model.user.UserSummaryDTO;
import com.efarm.efarmbackend.payload.request.auth.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.auth.SignupUserRequest;
import com.efarm.efarmbackend.payload.request.user.UpdateUserRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface UserManagementService {

    List<UserDTO> getFarmUsersByFarmId();

    List<UserSummaryDTO> getActiveFarmUsersByFarmId();

    List<User> getUsersByFarmId(Integer farmId);

    List<User> getActiveUsersByFarmId(Integer farmId);

    @Transactional
    void updateUserDetails(Integer userId, UpdateUserRequest updateUserRequest);

    void updateUserProperties(User user, UpdateUserRequest updateUserRequest);

    @Transactional
    void deleteAllUsersForFarm(Farm farm);

    @Transactional
    void toggleUserActiveStatus(Integer userId);

    User createFarmOwner(SignupFarmRequest signUpFarmRequest);

    User createFarmUser(SignupUserRequest signUpUserRequest);

    Farm getUserFarmById(Long userId);

    List<User> getAllOwnersForFarm(Integer farmId);
}

