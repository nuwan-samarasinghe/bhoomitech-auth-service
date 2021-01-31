package com.xcodel.authservice.config;

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

import javax.servlet.http.Cookie;
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
                .permitAll()
                .and()
                .logout(logout -> logout
                        .permitAll()
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            DefaultTokenServices defaultTokenServices = this.tokenServices();
                            defaultTokenServices.revokeToken(request.getHeader("authorization")
                                    .replaceFirst("Bearer", "").trim());
                            defaultTokenServices.setAccessTokenValiditySeconds(0);
                            defaultTokenServices.setRefreshTokenValiditySeconds(0);
                            HttpSession session;
                            SecurityContextHolder.clearContext();
                            session = request.getSession(true);
                            if (session != null) {
                                session.invalidate();
                            }
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        }));
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
        auth.inMemoryAuthentication().and().userDetailsService(userDetailsService)
                .passwordEncoder(getPasswordEncoder());
    }
}
