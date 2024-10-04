package com.efarm.efarmbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Aktualne hasło nie może być puste")
    private String currentPassword;

    @NotBlank(message = "Nowe hasło nie może być puste")
    @Size(min = 6, max = 40, message = "Nowe hasło musi mieć od 6 do 40 znaków")
    private String newPassword;
}
