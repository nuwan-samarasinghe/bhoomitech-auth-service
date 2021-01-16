package com.xcodel.authservice.repository;

import com.xcodel.authservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<Role, Integer> {
}
