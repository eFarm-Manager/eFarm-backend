package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.SignupRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.RoleRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.security.jwt.AuthEntryPointJwt;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivationCodeService activationCodeService;

    @Autowired
    private FarmService farmService;

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);


    @Transactional
    public ResponseEntity<?> registerUser(SignupRequest signUpRequest) {

        logger.info("Received signup User request: {}", signUpRequest);
        //Check user data
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
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

        if (strRole.equals("ROLE_FARM_OWNER")) {
            managerRole = roleRepository.findByName(ERole.ROLE_FARM_OWNER)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_OWNER is not found."));
        } else if (strRole.equals("ROLE_FARM_MANAGER")) {
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
        activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode());

        return ResponseEntity.ok(new MessageResponse("Farm registered successfully!"));
    }

}
