package com.efarm.efarmbackend.service.facades;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.service.FarmService;
import com.efarm.efarmbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FarmFacade {

    @Autowired
    private UserService userService;

    @Autowired
    private FarmService farmService;

    public ResponseEntity<List<UserDTO>> getFarmUsersByFarmId() {
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
}
