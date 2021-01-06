package com.auth.authservice.apidocs;

import lombok.Data;

import java.util.List;

@Data
public class NewUserDocument {
    private String email;
    private String password;
    private String userName;
    private List<Integer> roleIds;
}
