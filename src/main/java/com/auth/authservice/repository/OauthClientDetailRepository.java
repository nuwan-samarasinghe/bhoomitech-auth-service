package com.auth.authservice.repository;

import com.auth.authservice.model.AuthClientDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthClientDetailRepository extends JpaRepository<AuthClientDetails, String> {
}
