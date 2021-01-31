package com.xcodel.authservice.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Configuration
public class AuthWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;

    private final TokenStore getTokenStore;

    public AuthWebSecurityConfiguration(TokenStore getTokenStore,
                                        UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.getTokenStore = getTokenStore;
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(this.getTokenStore);
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers("/swagger-ui.html",
                        "/css/*",
                        "/img/*",
                        "/js/*",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        "/webjars/**",
                        "/register",
                        "/error",
                        "/activate",
                        "/forgot-password",
                        "/reset-password").permitAll()
                .antMatchers("/auth/user/**").permitAll()
                .antMatchers("/auth/user").permitAll()
                .antMatchers("/**").authenticated()
                .and()
                .formLogin().loginPage("/login")
                .defaultSuccessUrl("/redirect")
                .permitAll()
                .and()
                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessHandler((request, response, authentication) -> {
                            DefaultTokenServices defaultTokenServices = this.tokenServices();
                            String authorization = request.getHeader("authorization");
                            boolean inAuth = true;
                            if (StringUtils.isNotEmpty(authorization)) {
                                defaultTokenServices.revokeToken(authorization
                                        .replaceFirst("Bearer", "").trim());
                                inAuth = false;
                            }
                            defaultTokenServices.setAccessTokenValiditySeconds(0);
                            defaultTokenServices.setRefreshTokenValiditySeconds(0);
                            HttpSession session;
                            SecurityContextHolder.clearContext();
                            session = request.getSession(true);
                            if (session != null) {
                                session.invalidate();
                            }
                            if (inAuth) {
                                response.sendRedirect("/login");
                            } else {
                                response.setStatus(HttpServletResponse.SC_OK);
                            }
                        })
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true));
    }

    @Bean
    protected AuthenticationManager getAuthenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    PasswordEncoder getPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(getPasswordEncoder());
    }
}
