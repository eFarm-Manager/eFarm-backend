package com.efarm.efarmbackend.model.farm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FarmDTO {
    private String farmName;
    private String farmNumber;
    private String feedNumber;
    private String sanitaryRegisterNumber;

    private String street;
    private String buildingNumber;
    private String zipCode;
    private String city;

    private LocalDate activationCodeExpireDate;

    public FarmDTO(Farm farm, Address address, LocalDate activationCodeExpireDate) {
        this.farmName = farm.getFarmName();
        this.farmNumber = farm.getFarmNumber();
        this.feedNumber = farm.getFeedNumber();
        this.sanitaryRegisterNumber = farm.getSanitaryRegisterNumber();
        this.street = address.getStreet();
        this.buildingNumber = address.getBuildingNumber();
        this.zipCode = address.getZipCode();
        this.city = address.getCity();
        this.activationCodeExpireDate = activationCodeExpireDate;
    }
}