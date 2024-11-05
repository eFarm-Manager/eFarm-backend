package com.efarm.efarmbackend.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserSummaryDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String role;
}

