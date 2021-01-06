package com.auth.authservice.service;

import com.auth.authservice.apidocs.NewUserDocument;
import com.auth.authservice.model.User;
import com.auth.authservice.repository.UserRepository;
import com.auth.authservice.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public ResponseEntity<String> createNewUser(NewUserDocument newUserDocument) {
        User user = new User();
        user.setUsername(newUserDocument.getUserName());
        user.setPassword(passwordEncoder.encode(newUserDocument.getPassword()));
        user.setEnabled(Boolean.TRUE);
        user.setAccountNonExpired(Boolean.TRUE);
        user.setAccountNonLocked(Boolean.TRUE);
        user.setCredentialsNonExpired(Boolean.TRUE);
        user.setRoles(this.userRoleRepository.findAllById(newUserDocument.getRoleIds()));
        User saveUser = this.userRepository.save(user);
        log.info("USER : creation success full {}", saveUser);
        if (saveUser.getId() != null) {
            return ResponseEntity.ok("user created");
        } else {
            return ResponseEntity.ok("user not created");
        }
    }

    public User getUserById(Integer userId) {
        Optional<User> userOptional = this.userRepository.findById(userId);
        return userOptional.orElse(null);
    }
}
