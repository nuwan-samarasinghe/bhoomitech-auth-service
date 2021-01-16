package com.xcodel.authservice.repository;

import com.xcodel.authservice.model.AuthClientDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthClientDetailRepository extends JpaRepository<AuthClientDetails, String> {
}
