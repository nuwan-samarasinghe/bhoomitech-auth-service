package com.auth.authservice.repository;

import com.auth.authservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<Role, Integer> {
}
