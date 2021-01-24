package com.xcodel.authservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Configuration
public class AuthWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;

    public AuthWebSecurityConfiguration(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "**").permitAll()
                .antMatchers(
                        "/swagger-ui.html",
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
                        "/reset-password"
                ).permitAll()
                .antMatchers("/auth/user/**").permitAll()
                .antMatchers("/**").authenticated()
                .and()
                .formLogin().loginPage("/login").permitAll()
                .and()
                .logout(logout -> logout
                        .permitAll()
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            HttpSession session = request.getSession(false);
                            SecurityContextHolder.clearContext();
                            session = request.getSession(true);
                            if (session != null) {
                                session.invalidate();
                            }
                            response.setStatus(HttpServletResponse.SC_OK);
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
