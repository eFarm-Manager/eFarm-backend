package com.efarm.efarmbackend.service.user;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.model.user.UserSummaryDTO;
import com.efarm.efarmbackend.payload.request.auth.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.auth.SignupUserRequest;
import com.efarm.efarmbackend.payload.request.user.ChangeUserPasswordRequest;
import com.efarm.efarmbackend.payload.request.user.UpdateUserRequest;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createFarmOwner(SignupFarmRequest signUpFarmRequest);

    User createFarmUser(SignupUserRequest signUpUserRequest);

    User getLoggedUser();

    Farm getLoggedUserFarm();

    Farm getUserFarmById(Long userId);

    List<String> getLoggedUserRoles(UserDetailsImpl userDetails);

    Boolean isPasswordValidForLoggedUser(String providedPassword);

    void updatePasswordForLoggedUser(String newPassword);

    List<User> getAllOwnersForFarm(Integer farmId);

    Optional<User> getActiveUserById(UserDetailsImpl userDetails) throws RuntimeException;

    List<UserDTO> getFarmUsersByFarmId();

    List<UserSummaryDTO> getActiveFarmUsersByFarmId();

    @Transactional
    void deleteAllUsersForFarm(Farm farm);

    @Transactional
    void toggleUserActiveStatus(Integer userId);

    @Transactional
    void updateUserDetails(Integer userId, UpdateUserRequest updateUserRequest);

    @Transactional
    void updateUserPassword(Integer userId, ChangeUserPasswordRequest updatePasswordRequest);

    List<User> filterOperatorsForHelpNotifications(List<Integer> operatorIds, List<User> activeFarmOperators);

    List<User> filterInvalidOperatorsForHelpNotifications(List<Integer> operatorIds, List<User> activeFarmOperators);

    void updateUserProperties(User user, UpdateUserRequest updateUserRequest);

    List<User> getUsersByFarmId(Integer farmId);

    List<User> getActiveUsersByFarmId(Integer farmId);

    Role assignUserRole(String strRole);
}
