package com.xcodel.authservice.repository;

import com.xcodel.authservice.model.User;
import com.xcodel.authservice.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDetailRepository extends JpaRepository<UserDetail, Integer> {
    Optional<UserDetail> findByUser(User user);

    Optional<UserDetail> findByEmail(String email);
}
