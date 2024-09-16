package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ActivationCodeService {

    @Autowired
    private ActivationCodeRepository activationCodeRepository;

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
}

