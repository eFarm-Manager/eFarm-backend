package com.efarm.efarmbackend.service.landparcel;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.*;
import com.efarm.efarmbackend.repository.landparcel.LandOwnershipStatusRepository;
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LandparcelServiceImpl implements LandparcelService {

    private final LandOwnershipStatusRepository landOwnershipStatusRepository;
    private final LandparcelRepository landparcelRepository;

    @Override
    public void addNewLandparcelData(LandparcelDTO landparcelDTO, Landparcel landparcel) {
        ELandOwnershipStatus ownershipStatusEnum;
        try {
            ownershipStatusEnum = ELandOwnershipStatus.valueOf(landparcelDTO.getLandOwnershipStatus().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            ownershipStatusEnum = ELandOwnershipStatus.STATUS_LEASE;
        }
        LandOwnershipStatus ownershipStatus = landOwnershipStatusRepository.findByOwnershipStatus(ownershipStatusEnum)
                .orElseGet(() -> landOwnershipStatusRepository.findByOwnershipStatus(ELandOwnershipStatus.STATUS_LEASE).get());

        landparcel.setLandOwnershipStatus(ownershipStatus);
        setAdministrativeData(landparcel, landparcelDTO);
        setCommonFields(landparcel, landparcelDTO);
    }

    @Override
    public void updateLandparcelData(LandparcelDTO landparcelDTO, Landparcel landparcel) {
        if (landparcelDTO.getLandOwnershipStatus() != null) {
            ELandOwnershipStatus ownershipStatusEnum;
            try {
                ownershipStatusEnum = ELandOwnershipStatus.valueOf(landparcelDTO.getLandOwnershipStatus().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                ownershipStatusEnum = landparcel.getLandOwnershipStatus().getOwnershipStatus();
            }
            if (landparcel.getLandOwnershipStatus().getOwnershipStatus() != ownershipStatusEnum) {
                LandOwnershipStatus ownershipStatus = landOwnershipStatusRepository.findByOwnershipStatus(ownershipStatusEnum).get();
                landparcel.setLandOwnershipStatus(ownershipStatus);
            }
        }
        setCommonFields(landparcel, landparcelDTO);
    }

    @Override
    public Boolean isLandparcelAlreadyExistingByFarm(LandparcelDTO landparcelDTO, Farm loggedUserFarm) {
        return landparcelRepository.existsByGeodesyLandparcelNumberAndFarm(
                landparcelDTO.getGeodesyLandparcelNumber(),
                loggedUserFarm);
    }

    @Override
    public Boolean isLandparcelNameTaken(String name, Farm loggedUserFarm) {
        return landparcelRepository.existsByFarmAndName(loggedUserFarm, name);
    }

    @Override
    public Landparcel findlandparcelByFarm(Integer id, Farm loggedUserFarm) throws Exception {
        LandparcelId landparcelId = new LandparcelId(id, loggedUserFarm.getId());
        return landparcelRepository.findById(landparcelId)
                .orElseThrow(() -> new Exception("Nie znaleziono działki"));
    }

    @Override
    @Transactional
    public void deleteAllLandparcelsForFarm(Farm farm) {
        List<Landparcel> landparcels = landparcelRepository.findByFarmId(farm.getId());
        landparcelRepository.deleteAll(landparcels);
    }

    @Override
    public void setCommonFields(Landparcel landparcel, LandparcelDTO landparcelDTO) {
        if (landparcelDTO.getName() != null) {
            landparcel.setName(landparcelDTO.getName());
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

    @Override
    public void setAdministrativeData(Landparcel landparcel, LandparcelDTO landparcelDTO) {
        if (landparcelDTO.getVoivodeship() != null) {
            landparcel.setVoivodeship(landparcelDTO.getVoivodeship());
        }
        if (landparcelDTO.getDistrict() != null) {
            landparcel.setDistrict(landparcelDTO.getDistrict());
        }
        if (landparcelDTO.getCommune() != null) {
            landparcel.setCommune(landparcelDTO.getCommune());
        }
        if (landparcelDTO.getGeodesyDistrictNumber() != null) {
            landparcel.setGeodesyDistrictNumber(landparcelDTO.getGeodesyDistrictNumber());
        }
        if (landparcelDTO.getLandparcelNumber() != null) {
            landparcel.setLandparcelNumber(landparcelDTO.getLandparcelNumber());
        }
        if (landparcelDTO.getGeodesyLandparcelNumber() != null) {
            landparcel.setGeodesyLandparcelNumber(landparcelDTO.getGeodesyLandparcelNumber());
        }
    }
}