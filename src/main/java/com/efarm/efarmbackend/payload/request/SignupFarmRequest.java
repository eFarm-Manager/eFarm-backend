package com.efarm.efarmbackend.payload.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupFarmRequest {
    @NotBlank
    @Size(min = 3, max = 30)
    private String firstName;

    @NotBlank
    @Size(min = 3, max = 40)
    private String lastName;

    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    @Size(min = 6, max = 20)
    private String phoneNumber;

    @NotBlank
    @Size(min = 6, max = 45)
    private String farmName;

    @NotBlank
    @Size(min = 6, max = 45)
    private String activationCode;

}

