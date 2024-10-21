package com.efarm.efarmbackend.model.landparcel;

import com.efarm.efarmbackend.payload.request.landparcel.AddLandparcelRequest;
import com.efarm.efarmbackend.payload.request.landparcel.UpdateLandparcelRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String geodesyLandparcelNumber;
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
        this.geodesyLandparcelNumber = landparcel.getGeodesyLandparcelNumber();
        this.longitude = landparcel.getLongitude();
        this.latitude = landparcel.getLatitude();
        this.area = landparcel.getArea();
    }

    public LandparcelDTO(AddLandparcelRequest addLandparcelRequest) {
        this.landOwnershipStatus = addLandparcelRequest.getLandOwnershipStatus();
        this.voivodeship = addLandparcelRequest.getVoivodeship();
        this.district = addLandparcelRequest.getDistrict();
        this.commune = addLandparcelRequest.getCommune();
        this.geodesyRegistrationDistrictNumber = addLandparcelRequest.getGeodesyRegistrationDistrictNumber();
        this.landparcelNumber = addLandparcelRequest.getLandparcelNumber();
        this.geodesyLandparcelNumber = addLandparcelRequest.getGeodesyLandparcelNumber();
        this.longitude = addLandparcelRequest.getLongitude();
        this.latitude = addLandparcelRequest.getLatitude();
        this.area = addLandparcelRequest.getArea();
    }

    public LandparcelDTO(UpdateLandparcelRequest updateLandparcelRequest) {
        this.landOwnershipStatus = updateLandparcelRequest.getLandOwnershipStatus();
        this.longitude = updateLandparcelRequest.getLongitude();
        this.latitude = updateLandparcelRequest.getLatitude();
        this.area = updateLandparcelRequest.getArea();
    }
}
