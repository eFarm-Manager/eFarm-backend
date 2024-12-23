package com.efarm.efarmbackend.controller;


import com.efarm.efarmbackend.exception.TooManyRequestsException;
import com.efarm.efarmbackend.exception.UnauthorizedException;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.auth.LoginRequest;
import com.efarm.efarmbackend.payload.request.auth.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.auth.SignupUserRequest;
import com.efarm.efarmbackend.payload.request.auth.UpdateActivationCodeRequest;
import com.efarm.efarmbackend.payload.request.farm.UpdateActivationCodeByLoggedOwnerRequest;
import com.efarm.efarmbackend.payload.request.user.ChangePasswordRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.payload.response.UserInfoResponse;
import com.efarm.efarmbackend.security.jwt.JwtUtils;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.auth.AuthFacade;
import com.efarm.efarmbackend.service.auth.AuthService;
import com.efarm.efarmbackend.service.farm.ActivationCodeService;
import com.efarm.efarmbackend.service.farm.FarmService;
import com.efarm.efarmbackend.service.user.UserAuthenticationService;
import com.efarm.efarmbackend.service.user.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@RequestMapping("/auth")
public class AuthController {

    private final AuthFacade authFacade;
    private final JwtUtils jwtUtils;
    private final UserAuthenticationService userAuthenticationService;
    private final UserManagementService userManagementService;
    private final ActivationCodeService activationCodeService;
    private final FarmService farmService;
    private final AuthService authService;
    private final ValidationRequestService validationRequestService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        logger.info("Received signin request from user: {}", loginRequest.getUsername());
        try {
            validationRequestService.validateRequest(bindingResult);
            UserDetailsImpl userDetails = authService.authenticateUserByLoginRequest(loginRequest);
            List<String> roles = userAuthenticationService.getLoggedUserRoles(userDetails);

            Optional<User> loggingUser = userAuthenticationService.getActiveUserById(userDetails);
            Farm userFarm = userManagementService.getUserFarmById(Long.valueOf(userDetails.getId()));
            Role role = loggingUser.get().getRole();

            farmService.checkFarmDeactivation(userFarm, role);
            String expireCodeInfo = activationCodeService.generateExpireCodeInfo(userFarm, roles);

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(userDetails).toString())
                    .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),
                            userDetails.getEmail(), roles, expireCodeInfo));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/signup")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupUserRequest signUpUserRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            authFacade.registerUser(signUpUserRequest);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie zarejestrowano nowego użytkownika"));
        } catch (Exception e) {
            Farm farm = userAuthenticationService.getLoggedUserFarm();
            logger.error("Can not create user for farm: {}", farm.getId());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/signupfarm")
    public ResponseEntity<MessageResponse> registerFarmAndFarmOwner(@Valid @RequestBody SignupFarmRequest signupFarmRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            authFacade.registerFarmAndFarmOwner(signupFarmRequest);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie zarejestrowano nową farmę"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/update-activation-code")
    public ResponseEntity<MessageResponse> updateActivationCode(@Valid @RequestBody UpdateActivationCodeRequest updateActivationCodeRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            authFacade.updateActivationCode(updateActivationCodeRequest);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie zaktualizowano kod aktywacyjny"));
        } catch (TooManyRequestsException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse(e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/update-activation-code")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> updateActivationCodeByLoggedOwner(@Valid @RequestBody UpdateActivationCodeByLoggedOwnerRequest updateActivationCodeByLoggedOwnerRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            authFacade.updateActivationCodeByLoggedOwner(updateActivationCodeByLoggedOwnerRequest);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie zaktualizowano kod aktywacyjny"));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_EQUIPMENT_OPERATOR')")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            authFacade.changePassword(changePasswordRequest);
            return ResponseEntity.ok(new MessageResponse("Hasło zostało pomyślnie zmienione"));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("Wylogowano"));
    }
}
