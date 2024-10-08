package com.efarm.efarmbackend.service.landparcel;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import com.efarm.efarmbackend.model.landparcel.LandparcelId;
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LandparcelFacade {

    @Autowired
    private LandparcelService landparcelService;

    @Autowired
    private LandparcelRepository landparcelRepository;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(LandparcelFacade.class);

    @Transactional
    public void addNewLandparcel(LandparcelDTO landparcelDTO) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        LandparcelId landparcelId = new LandparcelId(landparcelRepository.findNextFreeIdForFarm(loggedUserFarm.getId()), loggedUserFarm.getId());
        Landparcel landparcel = new Landparcel(landparcelId, loggedUserFarm);

        if (landparcelRepository.existsByDistrictAndCommuneAndAndGeodesyRegistrationDistrictNumberAndLandparcelNumberAndFarm(
                landparcelDTO.getDistrict(),
                landparcelDTO.getCommune(),
                landparcelDTO.getGeodesyRegistrationDistrictNumber(),
                landparcelDTO.getLandparcelNumber(),
                loggedUserFarm)) {
            throw new Exception("Działka o powyższych danych geodezyjnych już istnieje!");
        }

        landparcelService.setLandparcelData(landparcelDTO, landparcel);
        landparcelRepository.save(landparcel);
    }

    public LandparcelDTO getLandparcelDetails(Integer id) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        LandparcelId landparcelId = new LandparcelId(id, loggedUserFarm.getId());

        Landparcel landparcel = landparcelRepository.findById(landparcelId)
                .orElseThrow(() -> new Exception("Działka o id: " + landparcelId.getId() + " nie została znaleziona"));

        if (!landparcel.getIsAvailable()) {
            throw new Exception("Wybrana działka już nie istnieje");
        }

        return landparcelService.createDTOtoDisplay(landparcel);
    }
}