package com.efarm.efarmbackend.payload.request.auth;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 30)
    private String firstName;

    @NotBlank
    @Size(min = 3, max = 40)
    private String lastName;

    @NotBlank
    @Size(min = 6, max = 30)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 17, max = 28)
    private String role;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @Size(max = 12)
    private String phoneNumber;

}
