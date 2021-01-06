package com.auth.authservice.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "oauth_client_details")
public class AuthClientDetails {
    @Id
    @Column(nullable = false)
    private String clientId;
    @Column(nullable = false)
    private String clientSecret;
    private String webServerRedirectUri;
    private String scope;
    private Integer accessTokenValidity;
    private Integer refreshTokenValidity;
    private String resourceIds;
    private String authorizedGrantTypes;
    private String authorities;
    private String additionalInformation;
    private String autoapprove;
}
