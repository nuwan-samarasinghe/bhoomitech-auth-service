package com.xcodel.authservice.model;

public class OauthException extends RuntimeException {

    public OauthException(String message) {
        super(message);
    }

    public OauthException(String message, Throwable exception) {
        super(message, exception);
    }
}
