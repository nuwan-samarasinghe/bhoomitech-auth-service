package com.xcodel.authservice.controller;

import com.xcodel.authservice.exception.AuthServiceException;
import com.xcodel.authservice.model.AuthClientDetails;
import com.xcodel.authservice.model.User;
import com.xcodel.authservice.service.OauthClientDetailsService;
import com.xcodel.authservice.service.UserService;
import com.xcodel.commons.auth.userdetail.UserDetailDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
        return "/error";
    }

    @GetMapping("/forgot-password")
    String forgotPassword() {
        return "html/forgot-password";
    }

    @GetMapping("/register")
    String register() {
        return "html/register";
    }


    @PostMapping("/forgot-password")
    RedirectView forgotPassword(@RequestParam String email, RedirectAttributes redirectAttrs) {
        RedirectView modelAndView = new RedirectView("/forgot-password", true);
        try {
            boolean success = userService.forgotPassword(email);
            if (success) {
                modelAndView = new RedirectView("/login", true);
                redirectAttrs.addFlashAttribute("success", "we have sent a password reset link to your email.");
            }
        } catch (AuthServiceException exception) {
            redirectAttrs.addFlashAttribute("error", exception.getMessage());
        }
        return modelAndView;
    }

    @PostMapping("/register")
    RedirectView register(@ModelAttribute UserDetailDocument userDetailDocument, RedirectAttributes redirectAttrs) {
        RedirectView modelAndView = new RedirectView("/register", true);
        try {
            userService.validateNewUser(userDetailDocument);
            // save user
            userService.createNewUser(userDetailDocument);
            modelAndView = new RedirectView("/login", true);
            redirectAttrs.addFlashAttribute("success", "Your account got created, please verify your email to activate.");
        } catch (AuthServiceException exception) {
            redirectAttrs.addFlashAttribute("error", exception.getMessage());
        } catch (Exception exception) {
            redirectAttrs.addFlashAttribute("error", "Unexpected error occurred.");
        }
        return modelAndView;
    }

    @GetMapping("/reset-password")
    ModelAndView resetPassword(@RequestParam String token, RedirectAttributes redirectAttrs) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("html/reset-password");
        try {
            userService.validateUserSecret(token, true);
        } catch (AuthServiceException exception) {
            modelAndView.setViewName("redirect:/login");
            modelAndView.addObject("error", exception.getMessage());
        }
        return modelAndView;
    }

    @PostMapping("/reset-password")
    RedirectView resetPassword(@RequestParam String token, @ModelAttribute String password, @ModelAttribute String confirmPassword, RedirectAttributes redirectAttrs) {
        RedirectView modelAndView = new RedirectView("/reset-password", true);
        try {
            User user = userService.validateUserSecret(token, true);
            userService.updatePassword(user, password, confirmPassword);
            modelAndView = new RedirectView("/login", true);
            redirectAttrs.addFlashAttribute("success", "You've successfully updated the password");
        } catch (AuthServiceException exception) {
            modelAndView = new RedirectView("/login", true);
            redirectAttrs.addFlashAttribute("error", exception.getMessage());
        }
        return modelAndView;
    }

    @GetMapping("/activate")
    ModelAndView activateUser(@RequestParam String token, RedirectAttributes redirectAttrs) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("html/activate");
        try {
            userService.validateUserSecret(token, false);
        } catch (AuthServiceException exception) {
            modelAndView.setViewName("redirect:/login");
            modelAndView.addObject("error", exception.getMessage());
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
