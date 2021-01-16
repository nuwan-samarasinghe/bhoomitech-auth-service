package com.xcodel.authservice.service;

import com.xcodel.authservice.model.AuthClientDetails;
import com.xcodel.authservice.model.OauthException;
import com.xcodel.authservice.repository.OauthClientDetailRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OauthClientDetailsService {
    private final OauthClientDetailRepository oauthClientDetailRepository;

    public OauthClientDetailsService(OauthClientDetailRepository oauthClientDetailRepository) {
        this.oauthClientDetailRepository = oauthClientDetailRepository;
    }

    public AuthClientDetails loadClientByClientId(String clientId) {
        Optional<AuthClientDetails> authClientDetails = this.oauthClientDetailRepository.findById(clientId);
        if (authClientDetails.isPresent()) {
            return authClientDetails.get();
        } else {
            throw new OauthException("Cannot find the client.");
        }
    }
}
