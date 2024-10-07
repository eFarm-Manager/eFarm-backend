package com.efarm.efarmbackend.service.landparcel;

import com.efarm.efarmbackend.model.landparcel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.stereotype.Service;

@Service
public class LandparcelService {

    private static final Logger logger = LoggerFactory.getLogger(LandparcelService.class);

    public void setLandparcelData(LandparcelDTO landparcelDTO, Landparcel landparcel) {
        if (landparcelDTO.getLandOwnershipStatus() != null) {
            try {
                ELandOwnershipStatus ownershipStatus = ELandOwnershipStatus.valueOf(landparcelDTO.getLandOwnershipStatus().toUpperCase());
                landparcel.setLandOwnershipStatus(new LandOwnershipStatus(ownershipStatus));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Niepoprawny status własności: " + landparcelDTO.getLandOwnershipStatus());
            }
        }
        if (landparcelDTO.getVoivodeship() != null) {
            landparcel.setVoivodeship(landparcelDTO.getVoivodeship());
        }
        if (landparcelDTO.getDistrict() != null) {
            landparcel.setDistrict(landparcelDTO.getDistrict());
        }
        if (landparcelDTO.getCommune() != null) {
            landparcel.setCommune(landparcelDTO.getCommune());
        }
        if (landparcelDTO.getGeodesyRegistrationDistrictNumber() != null) {
            landparcel.setGeodesyRegistrationDistrictNumber(landparcelDTO.getGeodesyRegistrationDistrictNumber());
        }
        if (landparcelDTO.getLandparcelNumber() != null) {
            landparcel.setLandparcelNumber(landparcelDTO.getLandparcelNumber());
        }
        if (landparcelDTO.getLongitude() != null) {
            landparcel.setLongitude(landparcelDTO.getLongitude());
        }
        if (landparcelDTO.getLatitude() != null) {
            landparcel.setLatitude(landparcelDTO.getLatitude());
        }
        if (landparcelDTO.getArea() != null && landparcelDTO.getArea() > 0) {
            landparcel.setArea(landparcelDTO.getArea());
        }
    }

    public LandparcelDTO createDTOtoDisplay(Landparcel landparcel) {
        LandparcelDTO landparcelDTO = new LandparcelDTO(landparcel);
        logger.info("Creating LandparcelDTO: {}", landparcelDTO);
        return landparcelDTO;
    }
}
