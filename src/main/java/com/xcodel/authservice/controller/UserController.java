package com.xcodel.authservice.controller;

import com.xcodel.authservice.model.User;
import com.xcodel.authservice.model.UserDetail;
import com.xcodel.authservice.service.UserService;
import com.xcodel.commons.auth.userdetail.UserDetailDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(value = "/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_operator')")
    @GetMapping(value = "/user/{userId}")
    public UserDetailDocument getUserById(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PutMapping(value = "/user/{userId}/activate")
    public ResponseEntity activateUser(@PathVariable String userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PutMapping(value = "/user/{userId}/deactivate")
    public ResponseEntity deactivateUser(@PathVariable String userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_operator')")
    @PostMapping(value = "/user")
    public User getUserByUserName(@RequestParam("userName") String userName) {
        return userService.getUserByUserName(userName);
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping(value = "/user/all")
    public List<UserDetailDocument> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.getAllUsers(authentication.getName());
    }

//    @PreAuthorize("hasRole('ROLE_admin')")
//    @PostMapping(value = "/user/create")
//    public ResponseEntity<String> createUsers(@RequestBody NewUserDocument newUserDocument) {
//        return userService.createNewUser(newUserDocument);
//    }


}
