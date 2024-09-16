package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.SignupFarmRequest;
import com.efarm.efarmbackend.repository.user.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;


    public User createFarmOwner(SignupFarmRequest signUpFarmRequest) {
        User user = new User(
                signUpFarmRequest.getFirstName(),
                signUpFarmRequest.getLastName(),
                signUpFarmRequest.getUsername(),
                signUpFarmRequest.getEmail(),
                encoder.encode(signUpFarmRequest.getPassword()),
                signUpFarmRequest.getPhoneNumber()
        );

        Role managerRole = roleRepository.findByName(ERole.ROLE_FARM_OWNER)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_FARM_OWNER is not found."));

        user.setRole(managerRole);

        return user;
    }
}

