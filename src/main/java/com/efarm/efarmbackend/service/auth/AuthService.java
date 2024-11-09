package com.efarm.efarmbackend.service.auth;

import com.efarm.efarmbackend.payload.request.auth.LoginRequest;
import com.efarm.efarmbackend.payload.request.auth.UpdateActivationCodeRequest;
import com.efarm.efarmbackend.security.services.BruteForceProtectionService;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private BruteForceProtectionService bruteForceProtectionService;

    public UserDetailsImpl authenticateUserByLoginRequest(LoginRequest loginRequest) {

        String username = loginRequest.getUsername();
        if (bruteForceProtectionService.isBlocked(username)) {
            throw new RuntimeException("Zbyt wiele nieudanych prób logowania. Spróbuj ponownie później.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            bruteForceProtectionService.loginSucceeded(username);
            return (UserDetailsImpl) authentication.getPrincipal();
        } catch (Exception e) {
            bruteForceProtectionService.loginFailed(username);
            throw new RuntimeException("Nieprawidłowe dane logowania");
        }
    }

    public UserDetailsImpl authenticateUserByUpdateCodeRequest(UpdateActivationCodeRequest updateActivationCodeRequest) {

        String username = updateActivationCodeRequest.getUsername();
        if (bruteForceProtectionService.isBlocked(username)) {
            throw new RuntimeException("Zbyt wiele nieudanych prób. Spróbuj ponownie później.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(updateActivationCodeRequest.getUsername(), updateActivationCodeRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            return (UserDetailsImpl) authentication.getPrincipal();
        } catch (Exception e) {
            bruteForceProtectionService.loginFailed(username);
            throw new RuntimeException("Nieprawidłowe dane logowania");
        }
    }

    public boolean hasCurrentUserRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(roleName));
    }
}
