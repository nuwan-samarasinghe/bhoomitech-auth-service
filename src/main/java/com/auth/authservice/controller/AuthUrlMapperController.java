package com.auth.authservice.controller;

import com.auth.authservice.model.AuthClientDetails;
import com.auth.authservice.service.OauthClientDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@SessionAttributes(types = AuthorizationRequest.class)
public class AuthUrlMapperController {
    private final OauthClientDetailsService oauthClientDetailsService;

    public AuthUrlMapperController(OauthClientDetailsService oauthClientDetailsService) {
        this.oauthClientDetailsService = oauthClientDetailsService;
    }

    @GetMapping("/login")
    String loginUser() {
        return "html/login";
    }

    @GetMapping("/error")
    String errorPageUrl() {
        return "html/error";
    }

    @GetMapping("/forgot-password")
    String forgotPassword() {
        return "html/forgot-password";
    }

    @GetMapping("/register")
    String register() {
        return "html/register";
    }

    @GetMapping("/reset-password/{token}")
    String resetPassword(@PathVariable("token") String token) {
        System.out.println(token);
        return "html/reset-password";
    }

    @GetMapping(value = "/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(@ModelAttribute AuthorizationRequest clientAuth) throws Exception {
        AuthClientDetails authClientDetails = oauthClientDetailsService.loadClientByClientId(clientAuth.getClientId());
        ModelAndView access_confirmation = new ModelAndView("access_confirmation");
        access_confirmation.addObject("auth_request", clientAuth);
        access_confirmation.addObject("client", authClientDetails);
        return access_confirmation;
    }
}
