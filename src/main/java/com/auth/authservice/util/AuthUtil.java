package com.auth.authservice.util;

import com.auth.authservice.model.UserMetaData;
import com.google.gson.Gson;
import com.xcodel.commons.secret.SecretUtil;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AuthUtil {

    public static @NonNull String generateUserSecret(@NonNull String userName, @NonNull String secretSeparator, UserMetaData userMetaData) throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        // setting secret preference to new user
        UserMetaData.Secret secret = new UserMetaData().new Secret();
        String secretSuffix = String.valueOf(Math.random());
        String secretWithUserInfo = SecretUtil.encrypt(String.format("%s%s%s", userName, secretSeparator, secretSuffix), SecretUtil.getKeyFromPassword("X)0DEL", "ledocx"));
        secret.setValidAfter(new Timestamp(System.currentTimeMillis()));
        Instant instant = Instant.now().plus(24, ChronoUnit.HOURS);
        secret.setValidBefore(Timestamp.from(instant));
        secret.setSecret(secretSuffix);
        userMetaData.setSecret(secret);
        return secretWithUserInfo;
    }

    public static @NonNull String convertUserMetaDataToJsonString(@NonNull UserMetaData userMetaData) {
        return new Gson().toJson(userMetaData);
    }

    public static @NonNull UserMetaData convertJsonStringToUserMetaData(@Nullable String userMetaSataString) {
        if (StringUtils.isEmpty(userMetaSataString)) {
            return new UserMetaData();
        }
        return new Gson().fromJson(userMetaSataString, UserMetaData.class);
    }
}
