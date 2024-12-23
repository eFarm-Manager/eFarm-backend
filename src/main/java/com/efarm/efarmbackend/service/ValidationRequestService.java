package com.efarm.efarmbackend.service;

import org.springframework.validation.BindingResult;

public interface ValidationRequestService {
    void validateRequest(BindingResult bindingResult) throws Exception;
}
