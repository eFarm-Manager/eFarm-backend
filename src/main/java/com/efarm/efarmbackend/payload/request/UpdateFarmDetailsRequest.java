package com.efarm.efarmbackend.payload.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateFarmDetailsRequest {
    @Size(min = 6, max = 45)
    private String farmName;

    @Size(max = 30)
    private String farmNumber;

    @Size(max = 30)
    private String feedNumber;

    @Size(max = 30)
    private String sanitaryRegisterNumber;

    @Size(max = 45)
    private String street;

    @Size(max = 5)
    private String buildingNumber;

    @Size(max = 6)
    private String zipCode;

    @Size(max = 45)
    private String city;
}
