package com.xcodel.authservice.repository;

import com.xcodel.authservice.model.User;
import com.xcodel.authservice.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailRepository extends JpaRepository<UserDetail, Integer> {
    Optional<User> findByUser(User user);

    Optional<User> findByEmail(String email);
}
