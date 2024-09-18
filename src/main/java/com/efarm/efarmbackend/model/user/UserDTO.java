package com.efarm.efarmbackend.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private String username;
    private String role;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive;
}