package com.efarm.efarmbackend.service.auth;

import com.efarm.efarmbackend.exception.UnauthorizedException;
import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.auth.*;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.efarm.efarmbackend.service.farm.ActivationCodeService;
import com.efarm.efarmbackend.service.farm.FarmService;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthFacade {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivationCodeService activationCodeService;

    @Autowired
    private FarmService farmService;

    @Autowired
    AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(AuthFacade.class);


    @Transactional
    public MessageResponse registerUser(SignupRequest signUpRequest) throws RuntimeException {

        logger.info("Received signup User request: {}", signUpRequest);
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Podana nazwa użytkownika jest już zajęta!");
        }
        User user = userService.createFarmUser(signUpRequest);
        Farm currentUserFarm = userService.getLoggedUserFarm();
        user.setFarm(currentUserFarm);
        userRepository.save(user);
        return new MessageResponse("Zarejestrowano nowego użytkownika!");
    }

    @Transactional
    public MessageResponse registerFarmAndFarmOwner(SignupFarmRequest signUpFarmRequest) throws RuntimeException {

        logger.info("Received signup Farm request: {}", signUpFarmRequest);
        if (userRepository.existsByUsername(signUpFarmRequest.getUsername())) {
            throw new RuntimeException("Wybrana nazwa użytkownika jest już zajęta!");
        }

        if (farmRepository.existsByFarmName(signUpFarmRequest.getFarmName())) {
            throw new RuntimeException("Wybrana nazwa farmy jest już zajęta!");
        }

        User user = userService.createFarmOwner(signUpFarmRequest);
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode());
        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode());
        Address address = new Address();
        addressRepository.save(address);
        Farm farm = farmService.createFarm(signUpFarmRequest.getFarmName(), address.getId(), activationCodeOpt.get().getId());
        user.setFarm(farm);

        userRepository.save(user);
        activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode());
        return new MessageResponse("Pomyślnie zarejestrowano nową farmę!");
    }

    @Transactional
    public ResponseEntity<?> updateActivationCode(UpdateActivationCodeRequest updateActivationCodeRequest) throws Exception {
        UserDetailsImpl userDetails = authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest);
        List<String> roles = userService.getLoggedUserRoles(userDetails);

        if (roles.contains("ROLE_FARM_OWNER")) {
            Farm userFarm = userService.getUserFarmById(Long.valueOf(userDetails.getId()));
            return ResponseEntity.ok(activationCodeService.updateActivationCodeForFarm(updateActivationCodeRequest.getNewActivationCode(), userFarm.getId(), userDetails.getUsername()));
        } else {
            throw new AccessDeniedException("Brak uprawnień");
        }
    }

    @Transactional
    public MessageResponse updateActivationCodeByLoggedOwner(UpdateActivationCodeByLoggedOwnerRequest updateActivationCodeByLoggedOwnerRequest) throws Exception {

        if (userService.isPasswordValidForLoggedUser(updateActivationCodeByLoggedOwnerRequest.getPassword())) {
            return activationCodeService.updateActivationCodeForFarm(updateActivationCodeByLoggedOwnerRequest.getNewActivationCode(), userService.getLoggedUserFarm().getId(), userService.getLoggedUser().getUsername());
        } else {
            throw new UnauthorizedException("Nieprawidłowe hasło");
        }
    }

    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest changePasswordRequest) {

        boolean isPasswordValid = userService.isPasswordValidForLoggedUser(changePasswordRequest.getCurrentPassword());
        if (!isPasswordValid) {
            throw new UnauthorizedException("Podano nieprawidłowe aktualne hasło");
        }
        if (!Objects.equals(changePasswordRequest.getCurrentPassword(), changePasswordRequest.getNewPassword())) {
            userService.updatePasswordForLoggedUser(changePasswordRequest.getNewPassword());
            return new MessageResponse("Hasło zostało pomyślnie zmienione");
        } else {
            throw new RuntimeException("Nowe hasło nie może być takie samo jak poprzednie");
        }
    }
}
