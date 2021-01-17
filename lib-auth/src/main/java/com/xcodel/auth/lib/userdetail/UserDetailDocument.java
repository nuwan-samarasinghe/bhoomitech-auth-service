package com.xcodel.auth.lib.userdetail;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class UserDetailDocument {

    private String id;

    private String email;

    private String name;

    private String address;

    private String telephone;

    private String organization;

    private String status;

    private String username;

    private String password;

    private String confirmPassword;
}
