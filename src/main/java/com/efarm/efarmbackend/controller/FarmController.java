package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.service.ActivationCodeService;
import com.efarm.efarmbackend.service.FarmService;
import com.efarm.efarmbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/farm")
public class FarmController {

    @Autowired
    private FarmService farmService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivationCodeService activationCodeService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<UserDTO>> getUsersByFarmId() {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        List<User> users = farmService.getUsersByFarmId(loggedUserFarm.getId());
        List<UserDTO> userDTOs = users.stream()
                .map(user -> new UserDTO(
                        user.getUsername(),
                        user.getRole().toString(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhoneNumber(),
                        user.getIsActive()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @PostMapping("/update-activation-code")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_MANAGER')")
    public ResponseEntity<?> updateActivationCode(@RequestParam String newActivationCode, @RequestParam Integer farmId) {
        //TODO farmId powinno być pobierane automatycznie
        return activationCodeService.updateActivationCodeForFarm(newActivationCode, farmId);
    }
}