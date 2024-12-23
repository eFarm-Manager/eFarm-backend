package com.efarm.efarmbackend.service.auth;

import com.efarm.efarmbackend.exception.UnauthorizedException;
import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.auth.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.auth.SignupUserRequest;
import com.efarm.efarmbackend.payload.request.auth.UpdateActivationCodeRequest;
import com.efarm.efarmbackend.payload.request.farm.UpdateActivationCodeByLoggedOwnerRequest;
import com.efarm.efarmbackend.payload.request.user.ChangePasswordRequest;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.efarm.efarmbackend.service.farm.ActivationCodeService;
import com.efarm.efarmbackend.service.farm.FarmService;
import com.efarm.efarmbackend.service.user.UserAuthenticationService;
import com.efarm.efarmbackend.service.user.UserManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthFacade {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final AddressRepository addressRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final AuthService authService;
    private final UserAuthenticationService userAuthenticationService;
    private final UserManagementService userManagementService;
    private final ActivationCodeService activationCodeService;
    private final FarmService farmService;
    final AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(AuthFacade.class);


    @Transactional
    public void registerUser(SignupUserRequest signUpUserRequest) throws RuntimeException {

        logger.info("Received signup User request: {}", signUpUserRequest);
        if (userRepository.existsByUsername(signUpUserRequest.getUsername())) {
            throw new RuntimeException("Podana nazwa użytkownika jest już zajęta");
        }
        User user = userManagementService.createFarmUser(signUpUserRequest);
        Farm currentUserFarm = userAuthenticationService.getLoggedUserFarm();
        user.setFarm(currentUserFarm);
        userRepository.save(user);
    }

    @Transactional
    public void registerFarmAndFarmOwner(SignupFarmRequest signUpFarmRequest) throws RuntimeException {

        logger.info("Received signup Farm request: {}", signUpFarmRequest);
        if (userRepository.existsByUsername(signUpFarmRequest.getUsername())) {
            throw new RuntimeException("Wybrana nazwa użytkownika jest już zajęta");
        }

        if (farmRepository.existsByFarmName(signUpFarmRequest.getFarmName())) {
            throw new RuntimeException("Wybrana nazwa farmy jest już zajęta");
        }

        User user = userManagementService.createFarmOwner(signUpFarmRequest);
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode());
        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode());
        Address address = new Address();
        addressRepository.save(address);
        Farm farm = farmService.createFarm(signUpFarmRequest.getFarmName(), address.getId(), activationCodeOpt.get().getId());
        user.setFarm(farm);

        userRepository.save(user);
        activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode());
    }

    @Transactional
    public void updateActivationCode(UpdateActivationCodeRequest updateActivationCodeRequest) throws Exception {
        UserDetailsImpl userDetails = authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest);
        List<String> roles = userAuthenticationService.getLoggedUserRoles(userDetails);

        if (roles.contains("ROLE_FARM_OWNER")) {
            Farm userFarm = userManagementService.getUserFarmById(Long.valueOf(userDetails.getId()));
            activationCodeService.updateActivationCodeForFarm(
                    updateActivationCodeRequest.getNewActivationCode(),
                    userFarm.getId(),
                    userDetails.getUsername()
            );
        } else {
            throw new AccessDeniedException("Brak uprawnień");
        }
    }

    @Transactional
    public void updateActivationCodeByLoggedOwner(UpdateActivationCodeByLoggedOwnerRequest updateActivationCodeByLoggedOwnerRequest) throws UnauthorizedException {
        if (userAuthenticationService.isPasswordValidForLoggedUser(updateActivationCodeByLoggedOwnerRequest.getPassword())) {
            activationCodeService.updateActivationCodeForFarm(
                    updateActivationCodeByLoggedOwnerRequest.getNewActivationCode(),
                    userAuthenticationService.getLoggedUserFarm().getId(),
                    userAuthenticationService.getLoggedUser().getUsername()
            );
        } else {
            throw new UnauthorizedException("Nieprawidłowe hasło");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest) {

        boolean isPasswordValid = userAuthenticationService.isPasswordValidForLoggedUser(changePasswordRequest.getCurrentPassword());
        if (!isPasswordValid) {
            throw new UnauthorizedException("Podano nieprawidłowe aktualne hasło");
        }
        if (!Objects.equals(changePasswordRequest.getCurrentPassword(), changePasswordRequest.getNewPassword())) {
            userAuthenticationService.updatePasswordForLoggedUser(changePasswordRequest.getNewPassword());
        } else {
            throw new RuntimeException("Nowe hasło nie może być takie samo jak poprzednie");
        }
    }
}
