package com.efarm.efarmbackend.model.landparcel;

import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LandparcelSummaryDTO {
    private Integer id;
    private String name;
    private Double longitude;
    private Double latitude;

    public LandparcelSummaryDTO(AgroActivity agroActivity) {
        this.id = agroActivity.getAgriculturalRecord().getLandparcel().getId().getId();
        this.name = agroActivity.getAgriculturalRecord().getLandparcel().getName();
        this.longitude = agroActivity.getAgriculturalRecord().getLandparcel().getLongitude();
        this.latitude = agroActivity.getAgriculturalRecord().getLandparcel().getLatitude();
    }
}
