package com.efarm.efarmbackend.controller;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.efarm.efarmbackend.domain.farm.ActivationCode;
import com.efarm.efarmbackend.domain.farm.Address;
import com.efarm.efarmbackend.domain.farm.Farm;
import com.efarm.efarmbackend.payload.request.SignupFarmRequest;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.security.jwt.AuthEntryPointJwt;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efarm.efarmbackend.domain.user.ERole;
import com.efarm.efarmbackend.domain.user.Role;
import com.efarm.efarmbackend.domain.user.User;
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
    ActivationCodeRepository activationcodeRepository;

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
    }

    @PostMapping("/signup")
    @Transactional
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        //Check user data
        logger.info("Received signup request: {}", signUpRequest);
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getPhoneNumber());

        //Set role for new User
        String strRole = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        Role managerRole;

        if (strRole.equals("ROLE_FARM_MANAGER")) {
            managerRole = roleRepository.findByName(ERole.ROLE_FARM_MANAGER)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_MANAGER is not found."));
        } else {
            managerRole = roleRepository.findByName(ERole.ROLE_FARM_EQUIPMENT_OPERATOR)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_EQUIPMENT_OPERATOR is not found."));
        }

        roles.add(managerRole);
        user.setRole(roles.iterator().next());

        // Set farmId
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        // Checking whether principal is of type UserDetailsImpl
        if (principal instanceof UserDetailsImpl currentUserDetails) {
            logger.info("Create user with principal: {}", principal);
            User currentUser = userRepository.findById(Long.valueOf(currentUserDetails.getId()))
                    .orElseThrow(() -> new RuntimeException("Error: Current user not found."));
            if (currentUser.getFarm() != null) {
                user.setFarm(currentUser.getFarm());
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Current user does not have a farm associated!"));
            }
        } else {
            logger.error("Can not create user with principal: {}", principal.toString());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Unexpected principal type."));
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }


    @Transactional
    @PostMapping("/signupfarm")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupFarmRequest signUpRequest) {

        //Check user data
        logger.info("Received signup request: {}", signUpRequest);
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getPhoneNumber());

        //Set role for new User
        Set<Role> roles = new HashSet<>();

        Role managerRole = roleRepository.findByName(ERole.ROLE_FARM_MANAGER)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_MANAGER is not found."));

        roles.add(managerRole);
        user.setRole(roles.iterator().next());

        //Check activation code
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(signUpRequest.getActivationCode());
        if (activationCodeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Activation code does not exist."));
        }

        if (activationCodeOpt.get().getExpireDate().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Activation code has expired."));
        }

        if (activationCodeOpt.get().getIsUsed()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Activation code has already been used."));
        }

        // Create farm
        Address address = new Address();
        addressRepository.save(address);
        Farm farm = new Farm(signUpRequest.getFarmName());
        farm.setIdAddress(address.getId());
        farm.setIdActivationCode(activationCodeOpt.get().getId());
        farmRepository.save(farm);
        user.setFarm(farm);

        ActivationCode activationCode = activationCodeOpt.get();
        activationCode.setIsUsed(true);
        activationCodeRepository.save(activationCode);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Farm registered successfully!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
}
