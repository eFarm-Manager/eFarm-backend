package com.efarm.efarmbackend.service.facades;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farm.FarmDTO;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.payload.request.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

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

    @Transactional
    public ResponseEntity<?> updateFarmDetails(UpdateFarmDetailsRequest updateFarmDetailsRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(new MessageResponse(String.join(", ", errorMessages)));
        }

        Farm loggedUserFarm = userService.getLoggedUserFarm();
        farmService.updateFarmDetails(loggedUserFarm, updateFarmDetailsRequest);

        Address address = addressService.findAddressById(loggedUserFarm.getIdAddress());
        addressService.updateFarmAddress(address, updateFarmDetailsRequest);

        return ResponseEntity.ok(new MessageResponse("Poprawnie zaktualizowamo dane gospodarstwa"));
    }
}
