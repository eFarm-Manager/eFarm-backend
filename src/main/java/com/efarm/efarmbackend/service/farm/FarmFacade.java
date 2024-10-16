package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farm.FarmDTO;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.auth.AuthService;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<UserDTO> getFarmUsersByFarmId() {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        List<User> users = farmService.getUsersByFarmId(loggedUserFarm.getId());
        return users.stream()
                .map(user -> new UserDTO(
                        user.getUsername(),
                        user.getRole().toString(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhoneNumber(),
                        user.getIsActive()))
                .collect(Collectors.toList());
    }

    public FarmDTO getFarmDetails() {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        Address address = addressService.findAddressById(loggedUserFarm.getId());
        ActivationCode activationCode = activationCodeService.findActivationCodeById(loggedUserFarm.getIdActivationCode());
        LocalDate expireDate = authService.hasCurrentUserRole("ROLE_FARM_OWNER") ? activationCode.getExpireDate() : null;

        return new FarmDTO(
                loggedUserFarm.getFarmName(),
                loggedUserFarm.getFarmNumber(),
                loggedUserFarm.getFeedNumber(),
                loggedUserFarm.getSanitaryRegisterNumber(),
                address.getStreet(),
                address.getBuildingNumber(),
                address.getZipCode(),
                address.getCity(),
                expireDate);
    }

    @Transactional
    public MessageResponse updateFarmDetails(UpdateFarmDetailsRequest updateFarmDetailsRequest) {

        Farm loggedUserFarm = userService.getLoggedUserFarm();
        farmService.updateFarmDetails(loggedUserFarm, updateFarmDetailsRequest);
        Address address = addressService.findAddressById(loggedUserFarm.getIdAddress());
        addressService.updateFarmAddress(address, updateFarmDetailsRequest);

        return new MessageResponse("Poprawnie zaktualizowamo dane gospodarstwa");
    }
}
