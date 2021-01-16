package com.xcodel.authservice.model.tockenstore;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "oauth_client_token")
public class OauthClientToken {
    @Id
    private String authenticationId;
    private String tokenId;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] token;
    private String userName;
    private String clientId;
}
