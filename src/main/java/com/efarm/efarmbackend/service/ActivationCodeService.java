package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.payload.response.UserInfoResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.security.jwt.JwtUtils;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ActivationCodeService {

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${efarm.app.notification.daysToShowExpireActivationCode}")
    private int daysToShowExpireActivationCodeNotification;

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

    public ResponseEntity<?> signinWithExpireCodeInfo(UserDetailsImpl userDetails, Farm userFarm, List<String> roles) {
        ActivationCode activationCode = findActivationCodeByFarmId(userFarm.getId());
        long daysToExpiration = ChronoUnit.DAYS.between(LocalDate.now(), activationCode.getExpireDate());

        if (daysToExpiration <= daysToShowExpireActivationCodeNotification && daysToExpiration >= 0) {
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(userDetails).toString())
                    .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),
                            userDetails.getEmail(), roles, "Kod aktywacyjny wygasa za " + daysToExpiration + " dni."));
        }
        return null;
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
    @Transactional
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

        // Pobranie aktualnego kodu aktywacyjnego
        Integer currentActivationCodeId = farm.getIdActivationCode();

        Optional<ActivationCode> currentActivationCodeOpt = activationCodeRepository.findById(currentActivationCodeId);
        if (currentActivationCodeOpt.isPresent()) {
            ActivationCode currentActivationCodeEntity = currentActivationCodeOpt.get();
            activationCodeRepository.delete(currentActivationCodeEntity);
        }


        // Przypisanie nowego kodu aktywacyjnego do farmy
        farm.setIdActivationCode(newActivationCodeEntity.getId());
        farm.setIsActive(true);  // Aktywowanie farmy po wprowadzeniu nowego kodu

        // Zaktualizowanie stanu farmy w bazie danych
        farmRepository.save(farm);

        // Oznaczenie nowego kodu jako użyty
        newActivationCodeEntity.setIsUsed(true);
        activationCodeRepository.save(newActivationCodeEntity);

        return ResponseEntity
                .status(HttpStatus.OK)
                .location(URI.create("/"))
                .body(new MessageResponse("Activation code updated successfully for the farm."));
    }
}

