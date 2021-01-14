package com.xcodel.auth.lib.userdetail;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class UserDetailDocument {

    private Integer id;

    private String email;

    private String name;

    private String address;

    private String telephone;

    private String organization;

    private String userName;

    private String password;

    private String confirmPassword;
}
