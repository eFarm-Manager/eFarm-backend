package com.efarm.efarmbackend.model.landparcel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.message.StringFormattedMessage;

@Getter
@Setter
@NoArgsConstructor
public class LandparcelDTO
{
    private Integer id;
    private String landOwnershipStatus;
    private String voivodeship;
    private String district;
    private String commune;
    private String geodesyRegistrationDistrictNumber;
    private String landparcelNumber;
    private Double longitude;
    private Double latitude;
    private Double area;

    public LandparcelDTO(Landparcel landparcel) {
        this.id = landparcel.getId().getId();
        this.landOwnershipStatus = landparcel.getLandOwnershipStatus().toString();
        this.voivodeship = landparcel.getVoivodeship();
        this.district = landparcel.getDistrict();
        this.commune = landparcel.getCommune();
        this.geodesyRegistrationDistrictNumber = landparcel.getGeodesyRegistrationDistrictNumber();
        this.landparcelNumber = landparcel.getLandparcelNumber();
        this.longitude = landparcel.getLongitude();
        this.latitude = landparcel.getLatitude();
        this.area = landparcel.getArea();
    }
}
