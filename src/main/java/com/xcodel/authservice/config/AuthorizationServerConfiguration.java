package com.xcodel.authservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.sql.DataSource;

@Configuration
public class AuthorizationServerConfiguration implements AuthorizationServerConfigurer {

    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    private final AuthenticationManager authenticationManager;

    private final TokenStore getTokenStore;

    private final UserDetailsService userDetailsService;

    public AuthorizationServerConfiguration(
            PasswordEncoder passwordEncoder,
            DataSource dataSource,
            TokenStore getTokenStore,
            AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.dataSource = dataSource;
        this.getTokenStore = getTokenStore;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.checkTokenAccess("isAuthenticated()").tokenKeyAccess("permitAll()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource).passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoint) {
        endpoint.authenticationManager(this.authenticationManager);
        endpoint.userDetailsService(this.userDetailsService);
        endpoint.tokenStore(getTokenStore);
    }
}
