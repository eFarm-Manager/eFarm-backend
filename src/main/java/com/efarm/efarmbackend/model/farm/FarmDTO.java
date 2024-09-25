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


    public FarmDTO(String farmName, String farmNumber, String feedNumber, String sanitaryRegisterNumber, String street, String buildingNumber, String zipCode, String city) {
        this.farmName = farmName;
        this.farmNumber = farmNumber;
        this.feedNumber = feedNumber;
        this.sanitaryRegisterNumber = sanitaryRegisterNumber;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.zipCode = zipCode;
        this.city = city;
    }
}
