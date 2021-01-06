package com.auth.authservice.model.tockenstore;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "oauth_access_token")
public class OauthAccessToken {
    @Id
    private String authenticationId;
    private String tokenId;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] token;
    private String userName;
    private String clientId;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] authentication;
    private String refreshToken;
}
