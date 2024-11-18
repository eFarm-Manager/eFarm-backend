package com.efarm.efarmbackend.payload.request.user;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeUserPasswordRequest {

    @NotBlank(message = "Nowe hasło nie może być puste")
    @Size(min = 6, max = 40, message = "Hasło musi mieć od 6 do 40 znaków")
    private String newPassword;
}
