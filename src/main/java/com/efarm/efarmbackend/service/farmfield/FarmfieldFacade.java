package com.efarm.efarmbackend.service.farmfield;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.farmfield.Farmfield;
import com.efarm.efarmbackend.model.farmfield.FarmfieldId;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelHasFarmfield;
import com.efarm.efarmbackend.model.landparcel.LandparcelId;
import com.efarm.efarmbackend.payload.request.farmfield.MergeLandparcelsRequest;
import com.efarm.efarmbackend.repository.farmfield.FarmfieldRepository;
import com.efarm.efarmbackend.repository.landparcel.LandparcelHasFarmfieldRepository;
import com.efarm.efarmbackend.repository.landparcel.LandparcelRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmfieldFacade {

    @Autowired
    private UserService userService;

    @Autowired
    private FarmfieldRepository farmfieldRepository;

    @Autowired
    private LandparcelRepository landparcelRepository;

    @Autowired
    private LandparcelHasFarmfieldRepository landparcelHasFarmfieldRepository;


    @Transactional
    public void mergeLandparcels(MergeLandparcelsRequest request) throws Exception {

        Farm loggedUserFarm = userService.getLoggedUserFarm();
        FarmfieldId targedFarmfieldId = new FarmfieldId(farmfieldRepository.findNextFreeIdForFarm(loggedUserFarm.getId()), loggedUserFarm.getId());
//        Farmfield targetFarmfield = farmfieldRepository.findById(farmfieldId)
//                .orElseThrow(() -> new Exception("Pole docelowe nie istnieje"));

        //TODO zabezpieczyć przed scalaniem rozdzielonej działki???
        //TODO przestawić dotychczasowe pola na nieaktywne

        List<LandparcelId> landparcelIds = new java.util.ArrayList<>(List.of());
        Double targetArea = 0.0;

        for (Integer id : request.getLandparcelIds()) {
            LandparcelId landparcelId = new LandparcelId(id, loggedUserFarm.getId());
            Landparcel landparcel = landparcelRepository.findById(landparcelId).get();
            targetArea += landparcel.getArea();
            landparcelIds.add(landparcelId);
        }

        List<Landparcel> landparcels = landparcelRepository.findAllById(landparcelIds);
        Farmfield targetFarmfield = new Farmfield(targedFarmfieldId, loggedUserFarm, request.getName(), targetArea);

        farmfieldRepository.save(targetFarmfield);

        // Usuń stare relacje działek z polami
//        landparcelHasFarmfieldRepository.deleteByFarmfieldIdIn(request.getFarmfieldIds());

        // Utwórz nowe relacje między działkami a polem docelowym
        for (Landparcel landparcel : landparcels) {
            LandparcelHasFarmfield relation = new LandparcelHasFarmfield();
            relation.setLandparcel(landparcel);
            relation.setFarmField(targetFarmfield);
            relation.setFarmId(loggedUserFarm.getId());
            landparcelHasFarmfieldRepository.save(relation);
        }
//        farmfieldRepository.saveAndFlush(targetFarmfield);

    }
}
