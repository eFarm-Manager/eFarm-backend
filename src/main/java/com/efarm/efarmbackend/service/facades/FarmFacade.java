package com.efarm.efarmbackend.service.facades;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farm.FarmDTO;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FarmFacade {

    @Autowired
    private UserService userService;

    @Autowired
    private FarmService farmService;

    @Autowired
    private ActivationCodeService activationCodeService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AddressService addressService;

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

    public ResponseEntity<FarmDTO> getFarmDetails() {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        Address address = addressService.findAddressById(loggedUserFarm.getId());
        ActivationCode activationCode = activationCodeService.findActivationCodeById(loggedUserFarm.getIdActivationCode());

        LocalDate expireDate = authService.hasCurrentUserRole("ROLE_FARM_OWNER") ? activationCode.getExpireDate() : null;

        FarmDTO farmDetails = new FarmDTO(
                loggedUserFarm.getFarmName(),
                loggedUserFarm.getFarmNumber(),
                loggedUserFarm.getFeedNumber(),
                loggedUserFarm.getSanitaryRegisterNumber(),
                address.getStreet(),
                address.getBuildingNumber(),
                address.getZipCode(),
                address.getCity(),
                expireDate);

        return ResponseEntity.ok(farmDetails);
    }
}
