package com.efarm.efarmbackend.service.landparcel;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import com.efarm.efarmbackend.model.landparcel.LandparcelId;
import com.efarm.efarmbackend.payload.request.landparcel.AddLandparcelRequest;
import com.efarm.efarmbackend.payload.request.landparcel.UpdateLandparcelRequest;
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import com.efarm.efarmbackend.service.agriculturalrecords.AgriculturalRecordService;
import com.efarm.efarmbackend.service.agriculturalrecords.SeasonService;
import com.efarm.efarmbackend.service.user.UserAuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LandparcelFacade {

    private final LandparcelService landparcelService;
    private final LandparcelRepository landparcelRepository;
    private final UserAuthenticationService userService;
    private final AgriculturalRecordService agriculturalRecordService;
    private final SeasonService seasonService;

    @Transactional
    public void addNewLandparcel(AddLandparcelRequest addLandparcelRequest) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        LandparcelDTO landparcelDTO = new LandparcelDTO(addLandparcelRequest);

        if (landparcelService.isLandparcelAlreadyExistingByFarm(landparcelDTO, loggedUserFarm)) {
            throw new IllegalArgumentException("Działka o powyższych danych geodezyjnych już istnieje");
        }
        if (landparcelService.isLandparcelNameTaken(landparcelDTO.getName(), loggedUserFarm)) {
            throw new IllegalArgumentException("Działka o podanej nazwie już istnieje");
        }

        LandparcelId landparcelId = new LandparcelId(
                landparcelRepository.findNextFreeIdForFarm(loggedUserFarm.getId()),
                loggedUserFarm.getId()
        );
        Landparcel landparcel = new Landparcel(landparcelId, loggedUserFarm);
        landparcelService.addNewLandparcelData(landparcelDTO, landparcel);
        landparcelRepository.save(landparcel);
        agriculturalRecordService.createAgriculturalRecordForLandparcel(
                landparcel,
                loggedUserFarm,
                seasonService.getCurrentSeason()
        );
    }

    public LandparcelDTO getLandparcelDetails(Integer id) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        LandparcelId landparcelId = new LandparcelId(id, loggedUserFarm.getId());

        Landparcel landparcel = landparcelRepository.findById(landparcelId)
                .orElseThrow(() -> new Exception("Działka o id: " + landparcelId.getId() + " nie została znaleziona"));

        if (!landparcel.getIsAvailable()) {
            throw new RuntimeException("Wybrana działka już nie istnieje");
        }
        return new LandparcelDTO(landparcel);
    }

    @Transactional
    public void updateLandparcel(Integer id, UpdateLandparcelRequest updateLandparcelRequest) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        LandparcelId landparcelId = new LandparcelId(id, loggedUserFarm.getId());

        LandparcelDTO landparcelDTO = new LandparcelDTO(updateLandparcelRequest);

        Landparcel landparcel = landparcelRepository.findById(landparcelId)
                .orElseThrow(() -> new Exception("Działka nie istnieje"));

        if (!landparcel.getIsAvailable()) {
            throw new RuntimeException("Wybrana działka już nie istnieje");
        }
        if (!landparcel.getName().equals(landparcelDTO.getName()) &&
                landparcelService.isLandparcelNameTaken(landparcelDTO.getName(), loggedUserFarm)) {
            throw new IllegalArgumentException("Działka o podanej nazwie już istnieje");
        }

        landparcelService.updateLandparcelData(landparcelDTO, landparcel);
        landparcelRepository.save(landparcel);
    }

    @Transactional
    public void deleteLandparcel(Integer id) throws Exception {
        LandparcelId landparcelId = new LandparcelId(id, userService.getLoggedUserFarm().getId());
        Landparcel landparcel = landparcelRepository.findById(landparcelId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono działki o id: " + id));

        if (landparcel.getIsAvailable()) {
            landparcel.setIsAvailable(false);
            landparcelRepository.save(landparcel);
        } else {
            throw new Exception("Wybrana działka już nie istnieje");
        }
    }

    public List<LandparcelDTO> getAvailableLandparcels(String searchString, Double minArea, Double maxArea) {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        List<Landparcel> landparcels = landparcelRepository.findByFarmIdAndIsAvailableTrue(loggedUserFarm.getId());

        if (searchString != null && searchString.length() >= 3) {
            landparcels = landparcels.stream()
                    .filter(lp -> lp.getCommune().contains(searchString) ||
                            lp.getGeodesyDistrictNumber().contains(searchString) ||
                            lp.getLandparcelNumber().contains(searchString))
                    .collect(Collectors.toList());
        }

        if (minArea != null) {
            landparcels = landparcels.stream()
                    .filter(lp -> lp.getArea() >= minArea)
                    .collect(Collectors.toList());
        }

        if (maxArea != null) {
            landparcels = landparcels.stream()
                    .filter(lp -> lp.getArea() <= maxArea)
                    .collect(Collectors.toList());
        }

        return landparcels.stream()
                .map(LandparcelDTO::new)
                .collect(Collectors.toList());
    }
}