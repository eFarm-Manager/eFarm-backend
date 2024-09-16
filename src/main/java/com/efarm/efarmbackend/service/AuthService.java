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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
    public ResponseEntity<?> registerFarmUser(SignupFarmRequest signUpFarmRequest) {

        logger.info("Received signup Farm request: {}", signUpFarmRequest);
        //Check user and farm data
        if (userRepository.existsByUsername(signUpFarmRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (farmRepository.existsByFarmName(signUpFarmRequest.getFarmName())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Farm Name is already taken!"));
        }

        // Create new user's account
        User user = new User(
                signUpFarmRequest.getFirstName(),
                signUpFarmRequest.getLastName(),
                signUpFarmRequest.getUsername(),
                signUpFarmRequest.getEmail(),
                encoder.encode(signUpFarmRequest.getPassword()),
                signUpFarmRequest.getPhoneNumber());

        // Set role for new User
        Role managerRole = roleRepository.findByName(ERole.ROLE_FARM_OWNER)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_OWNER is not found."));
        user.setRole(managerRole);

        // Check activation code
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode());
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

        Farm farm = new Farm(signUpFarmRequest.getFarmName());
        farm.setIdAddress(address.getId());
        farm.setIdActivationCode(activationCodeOpt.get().getId());
        farm.setIsActive(true);
        farmRepository.save(farm);

        user.setFarm(farm);
        userRepository.save(user);

        // Update ActivationCode Properties
        ActivationCode activationCode = activationCodeOpt.get();
        activationCode.setIsUsed(true);
        activationCodeRepository.save(activationCode);

        return ResponseEntity.ok(new MessageResponse("Farm registered successfully!"));
    }

}
