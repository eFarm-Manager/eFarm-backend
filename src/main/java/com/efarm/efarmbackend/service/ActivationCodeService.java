package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ActivationCodeService {

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    private FarmRepository farmRepository;

    public ResponseEntity<MessageResponse> checkActivationCode(String activationCode) {
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(activationCode);
        if (activationCodeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Activation code does not exist."));
        }

        if (activationCodeOpt.get().getExpireDate().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Activation code has expired."));
        }

        if (activationCodeOpt.get().getIsUsed()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Activation code has already been used."));
        }
        return ResponseEntity.ok().build();
    }

    public void markActivationCodeAsUsed(String activationCode) {
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(activationCode);
        if (activationCodeOpt.isPresent()) {
            ActivationCode activationCodeEntity = activationCodeOpt.get();
            activationCodeEntity.setIsUsed(true);
            activationCodeRepository.save(activationCodeEntity);
        } else {
            throw new RuntimeException("Activation code not found");
        }
    }

    //TODO uporządkować od tego miejsca
    public ActivationCode findActivationCodeByFarmId(Integer farmId) {
        // Znajdujemy farmę na podstawie ID
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm with ID " + farmId + " not found."));

        // Pobieramy ID kodu aktywacyjnego z farmy
        Integer activationCodeId = farm.getIdActivationCode();

        // Szukamy kodu aktywacyjnego na podstawie ID
        return activationCodeRepository.findById(activationCodeId)
                .orElseThrow(() -> new RuntimeException("Activation code with ID " + activationCodeId + " not found."));
    }

    // Nowa metoda do wprowadzania nowego kodu aktywacyjnego
    public ResponseEntity<MessageResponse> updateActivationCodeForFarm(String newActivationCode, Integer farmId) {
        // Sprawdzenie, czy nowy kod aktywacyjny istnieje
        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(newActivationCode);
        if (activationCodeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("New activation code does not exist."));
        }

        ActivationCode newActivationCodeEntity = activationCodeOpt.get();

        // Sprawdzenie, czy nowy kod aktywacyjny nie został już użyty
        if (newActivationCodeEntity.getIsUsed()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Activation code has already been used."));
        }

        // Sprawdzenie, czy nowy kod aktywacyjny nie wygasł
        if (newActivationCodeEntity.getExpireDate().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Activation code has expired."));
        }

        // Pobranie powiązanej farmy
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found for id: " + farmId));

        // Przypisanie nowego kodu aktywacyjnego do farmy
        farm.setIdActivationCode(newActivationCodeEntity.getId());
        farm.setIsActive(true);  // Aktywowanie farmy po wprowadzeniu nowego kodu

        // Zaktualizowanie stanu farmy w bazie danych
        farmRepository.save(farm);

        // Oznaczenie nowego kodu jako użyty
        newActivationCodeEntity.setIsUsed(true);
        activationCodeRepository.save(newActivationCodeEntity);

        return ResponseEntity.ok(new MessageResponse("Activation code updated successfully for the farm."));
    }
}

