package com.efarm.efarmbackend.service.user;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.user.ChangeUserPasswordRequest;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserAuthenticationService {

    User getLoggedUser();

    Farm getLoggedUserFarm();

    List<String> getLoggedUserRoles(UserDetailsImpl userDetails);

    Boolean isPasswordValidForLoggedUser(String providedPassword);

    Optional<User> getActiveUserById(UserDetailsImpl userDetails) throws RuntimeException;

    Role assignUserRole(String strRole);

    void updatePasswordForLoggedUser(String newPassword);

    @Transactional
    void updateUserPassword(Integer userId, ChangeUserPasswordRequest updatePasswordRequest);
}
