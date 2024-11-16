package com.efarm.efarmbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidationRequestService {

    public void validateRequest(BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            throw new Exception(String.join(", ", errorMessages));
        }
    }
}

