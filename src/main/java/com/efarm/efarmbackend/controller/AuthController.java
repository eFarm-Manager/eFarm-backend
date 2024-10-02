package com.efarm.efarmbackend.controller;


import com.efarm.efarmbackend.payload.request.*;
import com.efarm.efarmbackend.service.facades.AuthFacade;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.security.jwt.JwtUtils;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthFacade authFacade;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authFacade.authenticateUser(loginRequest);
    }

    @PostMapping("/signup")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, BindingResult bindingResult) {
        return authFacade.registerUser(signUpRequest, bindingResult);
    }

    @PostMapping("/signupfarm")
    public ResponseEntity<?> registerFarmAndFarmOwner(@Valid @RequestBody SignupFarmRequest signUpRequest, BindingResult bindingResult) {
        return authFacade.registerFarmAndFarmOwner(signUpRequest, bindingResult);
    }

    @PostMapping("/update-activation-code")
    public ResponseEntity<?> updateActivationCode(@Valid @RequestBody UpdateActivationCodeRequest updateActivationCodeRequest) {
        return authFacade.updateActivationCode(updateActivationCodeRequest);
    }

    @PutMapping("/update-activation-code")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> updateActivationCodeByLoggedOwner(@Valid @RequestBody UpdateActivationCodeByLoggedOwnerRequest updateActivationCodeByLoggedOwnerRequest, BindingResult bindingResult) {
        return authFacade.updateActivationCodeByLoggedOwner(updateActivationCodeByLoggedOwnerRequest, bindingResult);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
}
