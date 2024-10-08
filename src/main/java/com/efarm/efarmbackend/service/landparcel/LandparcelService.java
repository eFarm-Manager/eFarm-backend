package com.efarm.efarmbackend.service.landparcel;

import com.efarm.efarmbackend.model.landparcel.ELandOwnershipStatus;
import com.efarm.efarmbackend.model.landparcel.LandOwnershipStatus;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import com.efarm.efarmbackend.repository.landparcel.LandOwnershipStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LandparcelService {

    @Autowired
    private LandOwnershipStatusRepository landOwnershipStatusRepository;

    private static final Logger logger = LoggerFactory.getLogger(LandparcelService.class);

    public void setLandparcelData(LandparcelDTO landparcelDTO, Landparcel landparcel) {
        if (landparcelDTO.getLandOwnershipStatus() != null) {
            ELandOwnershipStatus ownershipStatusEnum;
            try {
                ownershipStatusEnum = ELandOwnershipStatus.valueOf(landparcelDTO.getLandOwnershipStatus().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                ownershipStatusEnum = ELandOwnershipStatus.STATUS_LEASE;
            }
            LandOwnershipStatus ownershipStatus = landOwnershipStatusRepository.findByOwnershipStatus(ownershipStatusEnum)
                    .orElseGet(() -> landOwnershipStatusRepository.findByOwnershipStatus(ELandOwnershipStatus.STATUS_LEASE).get());

            landparcel.setLandOwnershipStatus(ownershipStatus);
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
        return new LandparcelDTO(landparcel);
    }
}
