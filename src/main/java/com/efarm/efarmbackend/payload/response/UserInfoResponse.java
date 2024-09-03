package com.efarm.efarmbackend.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class UserInfoResponse {
    @Setter
    private Integer id;
    @Setter
    private String username;
    @Setter
    private String email;
    private List<String> roles;

    public UserInfoResponse(Integer id, String username, String email, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

}
