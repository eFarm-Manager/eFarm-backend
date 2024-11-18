package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.model.user.UserSummaryDTO;
import com.efarm.efarmbackend.payload.request.user.ChangeUserPasswordRequest;
import com.efarm.efarmbackend.payload.request.user.UpdateUserRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ValidationRequestService validationRequestService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<UserDTO>> getFarmUsersByFarmId() {
        return ResponseEntity.ok(userService.getFarmUsersByFarmId());
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<UserSummaryDTO>> getActiveFarmUsersByFarmId() {
        return ResponseEntity.ok(userService.getActiveFarmUsersByFarmId());
    }

    @PatchMapping("/toggle-active/{userId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> toggleUserStatus(@PathVariable Integer userId) {
        try {
            userService.toggleUserActiveStatus(userId);
            return ResponseEntity.ok(new MessageResponse("Zmieniono status"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/update/{userId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> updateUserDetails(@PathVariable Integer userId, @Valid @RequestBody UpdateUserRequest updateUserRequest, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            userService.updateUserDetails(userId, updateUserRequest);
            return ResponseEntity.ok(new MessageResponse("Zaktualizowano dane użytkownika"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/update-password/{userId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> updateUserPassword(@PathVariable Integer userId, @RequestBody @Valid ChangeUserPasswordRequest request, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            userService.updateUserPassword(userId, request);
            return ResponseEntity.ok(new MessageResponse("Zmieniono hasło użytkownika"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}