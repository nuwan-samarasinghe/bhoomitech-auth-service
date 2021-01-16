package com.xcodel.authservice.controller;

import com.xcodel.authservice.model.User;
import com.xcodel.authservice.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping(value = "/user/{userId}")
    public User getUserById(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_operator')")
    @PostMapping(value = "/user")
    public User getUserByUserName(@RequestParam("userName") String userName) {
        return userService.getUserByUserName(userName);
    }

//    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping(value = "/user/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

//    @PreAuthorize("hasRole('ROLE_admin')")
//    @PostMapping(value = "/user/create")
//    public ResponseEntity<String> createUsers(@RequestBody NewUserDocument newUserDocument) {
//        return userService.createNewUser(newUserDocument);
//    }


}
