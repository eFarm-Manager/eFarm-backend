package com.efarm.efarmbackend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import com.efarm.efarmbackend.payload.response.MessageResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidationRequestService {

    public ResponseEntity<?> validateRequest(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(new MessageResponse(String.join(", ", errorMessages)));
        }
        return null;
    }

    public void validateRequestWithException(BindingResult bindingResult) throws Exception{
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            throw new Exception(String.join(", ", errorMessages));
        }
    }
}