package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.service.FarmService;
import com.efarm.efarmbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/farm")
public class FarmController {

    @Autowired
    private FarmService farmService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<UserDTO>> getFarmUsersByFarmId() {
        return farmService.getFarmUsersByFarmId();
    }
}