package com.efarm.efarmbackend.payload.request.farm;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateActivationCodeByLoggedOwnerRequest {
    @NotBlank
    private String password;

    @NotBlank
    private String newActivationCode;
}

