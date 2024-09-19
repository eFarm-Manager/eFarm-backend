package com.efarm.efarmbackend.controller;


import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.UpdateActivationCodeRequest;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.security.jwt.AuthEntryPointJwt;
import com.efarm.efarmbackend.service.ActivationCodeService;
import com.efarm.efarmbackend.service.AuthService;
import com.efarm.efarmbackend.service.UserService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.efarm.efarmbackend.payload.request.LoginRequest;
import com.efarm.efarmbackend.payload.request.SignupRequest;
import com.efarm.efarmbackend.payload.response.UserInfoResponse;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.user.RoleRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.security.jwt.JwtUtils;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    FarmRepository farmRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    ActivationCodeRepository activationCodeRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ActivationCodeService activationCodeService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Value("${efarm.app.notification.daysToShowExpireActivationCode}")
    private int daysToShowExpireActivationCodeNotification;

    @Value("${efarm.app.frontend.updateActivationCodeUri}")
    private String frontendUriToUpdateActivationCode;

    //TODO uporządkować poniższą funkcję
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        logger.info("Get signing request from user: {}", loginRequest.getUsername());

        // Uwierzytelnianie użytkownika
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Sprawdzenie, czy użytkownik jest aktywny

        Integer userId = userDetails.getId();
        Optional<User> loggingUser = userRepository.findById(Long.valueOf(userId));

        if (loggingUser.isPresent() && !loggingUser.get().getIsActive()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User is inactive."));
        }

        // Sprawdzenie powiązanego gospodarstwa
        Farm userFarm = userService.getUserFarmById(Long.valueOf(userDetails.getId()));

        if (userFarm != null) {
            ActivationCode activationCode = activationCodeService.findActivationCodeByFarmId(userFarm.getId());

            // Sprawdzenie, czy gospodarstwo jest aktywne
            if (!userFarm.getIsActive()) {
                if (roles.contains("ROLE_FARM_EQUIPMENT_OPERATOR") || roles.contains("ROLE_FARM_MANAGER")) {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Gospodarstwo jest nieaktywne."));
                }

                // Przypadek: właściciel farmy lub menedżer
                if (roles.contains("ROLE_FARM_OWNER")) {

                    // Logujemy użytkownika, ale przekierowujemy go do strony zmiany kodu aktywacyjnego
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .location(URI.create(frontendUriToUpdateActivationCode))
                            .body(new MessageResponse("Gospodarstwo jest nieaktywne. Podaj nowy kod aktywacyjny."));
                }
            }

            // Sprawdzenie, czy kod aktywacyjny wygasa w ciągu 14 dni
            long daysToExpiration = ChronoUnit.DAYS.between(LocalDate.now(), activationCode.getExpireDate());
            if (daysToExpiration <= daysToShowExpireActivationCodeNotification && daysToExpiration >= 0) {
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(userDetails).toString())
                        .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),
                                userDetails.getEmail(), roles, "Kod aktywacyjny wygasa za " + daysToExpiration + " dni."));
            }
        }

        // Generowanie tokenu JWT i zwracanie standardowej odpowiedzi
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),
                        userDetails.getEmail(), roles));
    }


    @PostMapping("/signup")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return authService.registerUser(signUpRequest);
    }

    @PostMapping("/signupfarm")
    public ResponseEntity<?> registerFarmUser(@Valid @RequestBody SignupFarmRequest signUpRequest) {
        return authService.registerFarmAndFarmOwner(signUpRequest);
    }

    @PostMapping("/update-activation-code")
    public ResponseEntity<?> updateActivationCode(@Valid @RequestBody UpdateActivationCodeRequest updateActivationCodeRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(updateActivationCodeRequest.getUsername(), updateActivationCodeRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (roles.contains("ROLE_FARM_OWNER")) {
            Farm userFarm = userService.getUserFarmById(Long.valueOf(userDetails.getId()));

            if (userFarm == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Farm not found for the current user."));
            }

            // Aktualizacja kodu aktywacyjnego
            return activationCodeService.updateActivationCodeForFarm(updateActivationCodeRequest.getNewActivationCode(), userFarm.getId());
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Brak uprawnień"));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
}
