package com.xcodel.authservice.repository;

import com.xcodel.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String userName);

    Optional<User> findByEmail(String email);
}
