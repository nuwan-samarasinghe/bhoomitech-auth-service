package com.xcodel.authservice.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class UserMetaData {

    private Secret secret;

    @Setter
    @Getter
    public class Secret {
        boolean valid = true;
        private Timestamp validAfter;
        private Timestamp validBefore;
        private String secret;
    }
}
