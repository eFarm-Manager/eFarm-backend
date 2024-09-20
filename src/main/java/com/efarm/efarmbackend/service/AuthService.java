package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.LoginRequest;
import com.efarm.efarmbackend.payload.request.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.SignupRequest;
import com.efarm.efarmbackend.payload.request.UpdateActivationCodeRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.payload.response.UserInfoResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.security.jwt.JwtUtils;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivationCodeService activationCodeService;

    @Autowired
    private FarmService farmService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    @Transactional
    public ResponseEntity<?> registerUser(SignupRequest signUpRequest) {

        logger.info("Received signup User request: {}", signUpRequest);

        //Check user data
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        // Create new user's account
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
    public ResponseEntity<?> registerFarmAndFarmOwner(SignupFarmRequest signUpFarmRequest) {

        logger.info("Received signup Farm request: {}", signUpFarmRequest);
        //Check user and farm data
        if (userRepository.existsByUsername(signUpFarmRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (farmRepository.existsByFarmName(signUpFarmRequest.getFarmName())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Farm Name is already taken!"));
        }

        // Create new owner's account
        User user = userService.createFarmOwner(signUpFarmRequest);

        // Check activation code
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode());
        ResponseEntity<MessageResponse> validationResponse = activationCodeService.checkActivationCode(signUpFarmRequest.getActivationCode());
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

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        logger.info("Received signin request from user: {}", loginRequest.getUsername());

        // Uwierzytelnianie użytkownika
        UserDetailsImpl userDetails = authenticateUserByLoginRequest(loginRequest);
        List<String> roles = userService.getLoggedUserRoles(userDetails);

        // Sprawdzenie, czy użytkownik jest aktywny
        Optional<User> loggingUser = userRepository.findById(Long.valueOf(userDetails.getId()));

        if (loggingUser.isPresent() && !loggingUser.get().getIsActive()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User is inactive."));
        }

        // Sprawdzenie powiązanego gospodarstwa
        Farm userFarm = userService.getUserFarmById(Long.valueOf(userDetails.getId()));
        Role role = loggingUser.get().getRole();

        ResponseEntity<?> checkFarmDeactivationResponse = farmService.checkFarmDeactivation(userFarm, role);
        if (checkFarmDeactivationResponse != null) {
            return checkFarmDeactivationResponse;
        }

        // Sprawdzenie, czy kod aktywacyjny wygasa w ciągu 14 dni
        ResponseEntity<?> signinWithExpireCodeInfo = activationCodeService.signinWithExpireCodeInfo(userDetails, userFarm, roles);
        if (signinWithExpireCodeInfo != null) {
            return signinWithExpireCodeInfo;
        }

        // Zwracanie standardowej odpowiedzi
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(userDetails).toString())
                .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),
                        userDetails.getEmail(), roles));
    }

    public ResponseEntity<?> updateActivationCode(UpdateActivationCodeRequest updateActivationCodeRequest) {
        UserDetailsImpl userDetails = authenticateUserByUpdateCodeRequest(updateActivationCodeRequest);
        List<String> roles = userService.getLoggedUserRoles(userDetails);

        if (roles.contains("ROLE_FARM_OWNER")) {
            Farm userFarm = userService.getUserFarmById(Long.valueOf(userDetails.getId()));
            return activationCodeService.updateActivationCodeForFarm(updateActivationCodeRequest.getNewActivationCode(), userFarm.getId());
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Brak uprawnień"));
        }
    }


    public UserDetailsImpl authenticateUserByLoginRequest(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    public UserDetailsImpl authenticateUserByUpdateCodeRequest(UpdateActivationCodeRequest updateActivationCodeRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(updateActivationCodeRequest.getUsername(), updateActivationCodeRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (UserDetailsImpl) authentication.getPrincipal();
    }
}
