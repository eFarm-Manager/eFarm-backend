package com.efarm.efarmbackend.payload.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserInfoResponse {

    private Integer id;

    private String username;

    private String email;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<String> roles;

    public UserInfoResponse(Integer id, String username, String email, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = new ArrayList<>(roles);
    }

    public List<String> getRoles() {
        return new ArrayList<>(this.roles);
    }

    public void setRoles(List<String> roles) {
        if (roles != null) {
            this.roles = new ArrayList<>(roles);
        } else {
            this.roles = new ArrayList<>();
        }
    }


}
