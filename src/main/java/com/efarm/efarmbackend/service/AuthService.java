package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.SignupFarmRequest;
import com.efarm.efarmbackend.payload.request.SignupRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.AddressRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

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

}
