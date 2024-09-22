package com.efarm.efarmbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateActivationCodeRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String newActivationCode;
}
