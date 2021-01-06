package com.auth.authservice.controller;

import com.auth.authservice.model.AuthClientDetails;
import com.auth.authservice.service.OauthClientDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
        log.info("redirecting the application to login page");
        return "login";
    }

    @GetMapping("/error")
    String errorPageUrl() {
        log.info("redirecting the application error page");
        return "error";
    }

    @GetMapping("/forgot-password")
    String forgotPassword() {
        return "forgot-password";
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
