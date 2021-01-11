package com.auth.authservice.controller;

import com.auth.authservice.model.AuthClientDetails;
import com.auth.authservice.model.User;
import com.auth.authservice.service.OauthClientDetailsService;
import com.auth.authservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

@Slf4j
@Controller
@SessionAttributes(types = AuthorizationRequest.class)
public class AuthUrlMapperController {

    private final OauthClientDetailsService oauthClientDetailsService;
    private final UserService userService;

    public AuthUrlMapperController(OauthClientDetailsService oauthClientDetailsService, UserService userService) {
        this.oauthClientDetailsService = oauthClientDetailsService;
        this.userService = userService;
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
    String forgotPassword(@RequestParam(required = false) String email) {
        userService.forgotPassword(email);
        return "html/forgot-password";
    }

    @GetMapping("/register")
    String register() {
        return "html/register";
    }

    @GetMapping("/reset-password")
    ModelAndView resetPassword(@RequestParam  String token) {
        User user = userService.validateUserSecret(token, true);
        ModelAndView modelAndView = new ModelAndView();
        if (Objects.isNull(user)) {
            modelAndView.setViewName("redirect:/login");
        } else {
            modelAndView.setViewName("html/reset-password");
        }
        return modelAndView;
    }

    @GetMapping(value = "/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(@ModelAttribute AuthorizationRequest clientAuth) throws Exception {
        AuthClientDetails authClientDetails = oauthClientDetailsService.loadClientByClientId(clientAuth.getClientId());
        ModelAndView access_confirmation = new ModelAndView("html/access_confirmation");
        access_confirmation.addObject("auth_request", clientAuth);
        access_confirmation.addObject("client", authClientDetails);
        return access_confirmation;
    }
}
