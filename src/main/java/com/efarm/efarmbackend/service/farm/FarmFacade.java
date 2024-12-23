package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farm.FarmDTO;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;
import com.efarm.efarmbackend.service.auth.AuthService;
import com.efarm.efarmbackend.service.user.UserAuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FarmFacade {

    private final UserAuthenticationService userAuthenticationService;
    private final FarmService farmService;
    private final ActivationCodeService activationCodeService;
    private final AuthService authService;
    private final AddressService addressService;

    public FarmDTO getFarmDetails() {
        Farm loggedUserFarm = userAuthenticationService.getLoggedUserFarm();
        Address address = addressService.findAddressById(loggedUserFarm.getId());
        ActivationCode activationCode = activationCodeService.findActivationCodeById(loggedUserFarm.getIdActivationCode());
        LocalDate expireDate = authService.hasCurrentUserRole("ROLE_FARM_OWNER") ? activationCode.getExpireDate() : null;
        return new FarmDTO(loggedUserFarm, address, expireDate);
    }

    @Transactional
    public void updateFarmDetails(UpdateFarmDetailsRequest updateFarmDetailsRequest) {
        Farm loggedUserFarm = userAuthenticationService.getLoggedUserFarm();
        farmService.updateFarmDetails(loggedUserFarm, updateFarmDetailsRequest);
        Address address = addressService.findAddressById(loggedUserFarm.getIdAddress());
        addressService.updateFarmAddress(address, updateFarmDetailsRequest);
    }
}