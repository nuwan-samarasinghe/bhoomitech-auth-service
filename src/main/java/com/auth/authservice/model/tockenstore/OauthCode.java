package com.auth.authservice.model.tockenstore;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "oauth_code")
public class OauthCode {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;
    private String code;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] authentication;
}
