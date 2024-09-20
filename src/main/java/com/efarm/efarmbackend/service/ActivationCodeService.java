package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.payload.response.UserInfoResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import com.efarm.efarmbackend.repository.farm.FarmRepository;
import com.efarm.efarmbackend.security.jwt.JwtUtils;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ActivationCodeService.class);

    public ResponseEntity<MessageResponse> validateActivationCode(String activationCode) {
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
        ActivationCode activationCode;
        try {
            activationCode = findActivationCodeByFarmId(userFarm.getId());
        } catch (RuntimeException e) {
            logger.error("Can not find activation code : {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
        long daysToExpiration = ChronoUnit.DAYS.between(LocalDate.now(), activationCode.getExpireDate());

        if (daysToExpiration <= daysToShowExpireActivationCodeNotification && daysToExpiration >= 0) {
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(userDetails).toString())
                    .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),
                            userDetails.getEmail(), roles, "Kod aktywacyjny wygasa za " + daysToExpiration + " dni."));
        }
        return null;
    }

    public ActivationCode findActivationCodeByFarmId(Integer farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm with ID " + farmId + " not found."));

        Integer activationCodeId = farm.getIdActivationCode();

        return activationCodeRepository.findById(activationCodeId)
                .orElseThrow(() -> new RuntimeException("Activation code with ID " + activationCodeId + " not found."));
    }

    public ResponseEntity<MessageResponse> updateActivationCodeForFarm(String newActivationCode, Integer farmId) {

        ResponseEntity<MessageResponse> validationResponse = validateActivationCode(newActivationCode);
        if (validationResponse.getStatusCode() != HttpStatus.OK) {
            return validationResponse;
        }

        Optional<ActivationCode> activationCodeOpt = activationCodeRepository.findByCode(newActivationCode);
        ActivationCode newActivationCodeEntity = activationCodeOpt.get();

        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found for id: " + farmId));

        Optional<ActivationCode> currentActivationCodeOpt = activationCodeRepository.findById(farm.getIdActivationCode());
        if (currentActivationCodeOpt.isPresent()) {
            ActivationCode currentActivationCodeEntity = currentActivationCodeOpt.get();
            activationCodeRepository.delete(currentActivationCodeEntity);
        }

        farm.setIdActivationCode(newActivationCodeEntity.getId());
        farm.setIsActive(true);

        farmRepository.save(farm);

        newActivationCodeEntity.setIsUsed(true);
        activationCodeRepository.save(newActivationCodeEntity);

        return ResponseEntity
                .status(HttpStatus.OK)
                .location(URI.create("/"))
                .body(new MessageResponse("Activation code updated successfully for the farm."));
    }
}

