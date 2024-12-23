package com.efarm.efarmbackend.service.farm;

import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.payload.request.farm.UpdateFarmDetailsRequest;

public interface AddressService {
    Address findAddressById(Integer codeId);

    void updateFarmAddress(Address address, UpdateFarmDetailsRequest updateFarmDetailsRequest);
}
