package com.efarm.efarmbackend.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
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

    public UserDTO(User user) {
        this.username = user.getUsername();
        this.role = user.getRole().toString();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phone = user.getPhoneNumber();
        this.isActive = user.getIsActive();
    }
}