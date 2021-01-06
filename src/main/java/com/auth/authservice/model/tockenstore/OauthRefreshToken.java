package com.auth.authservice.model.tockenstore;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "oauth_refresh_token")
public class OauthRefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String tokenId;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] token;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] authentication;
}

