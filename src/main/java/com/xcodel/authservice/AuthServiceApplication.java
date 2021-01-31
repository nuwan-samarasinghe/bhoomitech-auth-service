package com.xcodel.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

@SpringBootApplication
@EnableAuthorizationServer
public class AuthServiceApplication {

    private final DataSource dataSource;

    public AuthServiceApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    TokenStore getTokenStore() {
        return new JdbcTokenStore(dataSource);
    }

}
