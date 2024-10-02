package com.efarm.efarmbackend.service.facades;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.*;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.payload.response.UserInfoResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.security.jwt.JwtUtils;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.efarm.efarmbackend.service.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private ValidationRequestService validationRequestService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(AuthFacade.class);


    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {

        logger.info("Received signin request from user: {}", loginRequest.getUsername());
        try {
            UserDetailsImpl userDetails = authService.authenticateUserByLoginRequest(loginRequest);
            List<String> roles = userService.getLoggedUserRoles(userDetails);

            Optional<User> loggingUser = userRepository.findById(Long.valueOf(userDetails.getId()));
            if (loggingUser.isPresent() && !loggingUser.get().getIsActive()) {
                return ResponseEntity.badRequest().body(new MessageResponse("User is inactive."));
            }

            Farm userFarm = userService.getUserFarmById(Long.valueOf(userDetails.getId()));
            Role role = loggingUser.get().getRole();

            //Check farm deactivation status
            ResponseEntity<?> checkFarmDeactivationResponse = farmService.checkFarmDeactivation(userFarm, role);
            if (checkFarmDeactivationResponse != null) {
                return checkFarmDeactivationResponse;
            }

            //Send resposne with expireCodeInfo
            ResponseEntity<?> signinWithExpireCodeInfo = activationCodeService.signinWithExpireCodeInfo(userDetails, userFarm, roles);
            if (signinWithExpireCodeInfo != null) {
                return signinWithExpireCodeInfo;
            }

            // Standard response
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(userDetails).toString())
                    .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),
                            userDetails.getEmail(), roles));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> registerUser(SignupRequest signUpRequest, BindingResult bindingResult) {

        ResponseEntity<?> validationErrorResponse = validationRequestService.validateRequest(bindingResult);
        if (validationErrorResponse != null) {
            return validationErrorResponse;
        }

        logger.info("Received signup User request: {}", signUpRequest);
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        User user = userService.createFarmUser(signUpRequest);

        // Set the same farmId as currently logged user
        try {
            Farm currentUserFarm = userService.getLoggedUserFarm();
            user.setFarm(currentUserFarm);
        } catch (RuntimeException e) {
            logger.error("Can not create user with farm: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @Transactional
    public ResponseEntity<?> registerFarmAndFarmOwner(SignupFarmRequest signUpFarmRequest, BindingResult bindingResult) {

        ResponseEntity<?> validationErrorResponse = validationRequestService.validateRequest(bindingResult);
        if (validationErrorResponse != null) {
            return validationErrorResponse;
        }

        logger.info("Received signup Farm request: {}", signUpFarmRequest);
        if (userRepository.existsByUsername(signUpFarmRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (farmRepository.existsByFarmName(signUpFarmRequest.getFarmName())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Farm Name is already taken!"));
        }

        User user = userService.createFarmOwner(signUpFarmRequest);

        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode());
        ResponseEntity<MessageResponse> validationResponse = activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode());
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return validationResponse;
        }

        // Create farm
        Address address = new Address();
        addressRepository.save(address);

        Farm farm = farmService.createFarm(signUpFarmRequest.getFarmName(), address.getId(), activationCodeOpt.get().getId());

        user.setFarm(farm);
        userRepository.save(user);

        // Update ActivationCode Properties
        try {
            activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode());
        } catch (RuntimeException e) {
            logger.error("Can not use activation code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
        return ResponseEntity.ok(new MessageResponse("Farm registered successfully!"));
    }

    @Transactional
    public ResponseEntity<?> updateActivationCode(UpdateActivationCodeRequest updateActivationCodeRequest) {
        try {
            UserDetailsImpl userDetails = authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest);
            List<String> roles = userService.getLoggedUserRoles(userDetails);

            if (roles.contains("ROLE_FARM_OWNER")) {
                Farm userFarm = userService.getUserFarmById(Long.valueOf(userDetails.getId()));
                return activationCodeService.updateActivationCodeForFarm(updateActivationCodeRequest.getNewActivationCode(), userFarm.getId(), userDetails.getUsername());
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Brak uprawnień"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> updateActivationCodeByLoggedOwner(UpdateActivationCodeByLoggedOwnerRequest updateActivationCodeByLoggedOwnerRequest, BindingResult bindingResult) {

        ResponseEntity<?> validationErrorResponse = validationRequestService.validateRequest(bindingResult);
        if (validationErrorResponse != null) {
            return validationErrorResponse;
        }

        if (userService.isPasswordValidForLoggedUser(updateActivationCodeByLoggedOwnerRequest.getPassword())) {
            return activationCodeService.updateActivationCodeForFarm(updateActivationCodeByLoggedOwnerRequest.getNewActivationCode(), userService.getLoggedUserFarm().getId(), userService.getLoggedUser().getUsername());
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Nieprawidłowe hasło"));
        }
    }
}
