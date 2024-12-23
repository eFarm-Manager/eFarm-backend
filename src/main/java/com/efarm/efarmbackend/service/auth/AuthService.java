package com.efarm.efarmbackend.service.auth;

import com.efarm.efarmbackend.payload.request.auth.LoginRequest;
import com.efarm.efarmbackend.payload.request.auth.UpdateActivationCodeRequest;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;

public interface AuthService {
    UserDetailsImpl authenticateUserByLoginRequest(LoginRequest loginRequest);

    UserDetailsImpl authenticateUserByUpdateCodeRequest(UpdateActivationCodeRequest updateActivationCodeRequest);

    boolean hasCurrentUserRole(String roleName);
}
