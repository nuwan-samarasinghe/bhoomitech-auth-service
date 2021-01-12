package com.auth.authservice.service;

import com.auth.authservice.apidocs.NewUserDocument;
import com.auth.authservice.model.User;
import com.auth.authservice.model.UserMetaData;
import com.auth.authservice.repository.UserRepository;
import com.auth.authservice.repository.UserRoleRepository;
import com.auth.authservice.util.AuthUtil;
import com.google.gson.Gson;
import com.xcodel.commons.mail.MailService;
import com.xcodel.commons.mail.model.Email;
import com.xcodel.commons.mail.model.MailConfiguration;
import com.xcodel.commons.secret.SecretUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserRoleRepository userRoleRepository;
    private String domainName = "http://localhost:12002";
    private String secretSeparator = "somethinfornow";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public ResponseEntity<String> createNewUser(NewUserDocument newUserDocument) {
        User user = new User();
        user.setUsername(newUserDocument.getUserName());
        user.setPassword(passwordEncoder.encode(newUserDocument.getPassword()));
        user.setEnabled(Boolean.TRUE);
        user.setAccountNonExpired(Boolean.TRUE);
        user.setAccountNonLocked(Boolean.TRUE);
        user.setCredentialsNonExpired(Boolean.TRUE);
        user.setRoles(this.userRoleRepository.findAllById(newUserDocument.getRoleIds()));
        User saveUser = this.userRepository.save(user);
//        log.info("USER : creation success full {}", saveUser);
        if (saveUser.getId() != null) {
            return ResponseEntity.ok("user created");
        } else {
            return ResponseEntity.ok("user not created");
        }
    }

    public User getUserById(Integer userId) {
        Optional<User> userOptional = this.userRepository.findById(userId);
        return userOptional.orElse(null);
    }

    public @Nullable
    String forgotPassword(@Nullable String email) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            try {
                UserMetaData userMetaData = AuthUtil.convertJsonStringToUserMetaData(user.getUserMetaData());
                String resetPwSecret = AuthUtil.generateUserSecret(user.getUsername(),
                        secretSeparator,
                        userMetaData);
                user.setUserMetaData(AuthUtil.convertUserMetaDataToJsonString(userMetaData));
                userRepository.save(user);

                StringBuilder mailBody = new StringBuilder();
                mailBody.append("Hello ").append(user.getUsername())
                        .append("\n")
                        .append("please click the following link to reset your account password")
                        .append("\n")
                        .append(String.format("%s/reset-password/", domainName))
                        .append(resetPwSecret);
                sendMail(user.getUsername(), user.getEmail(), mailBody.toString());
            } catch (Exception e) {
                throw new RuntimeException("something went wrong");
            }

        }

        return null;
    }

    private Boolean sendMail(@NonNull String userName, @NonNull String emailAddress, @NonNull String body) {
        MailConfiguration mailConfiguration = new MailConfiguration();
        mailConfiguration.setSmtpHost("mail.bhoomitech.com");
        mailConfiguration.setSmtpPort(25);
        mailConfiguration.setSmtpUserName("gnsspp@bhoomitech.com");
        mailConfiguration.setSmtpPassword("D,ISp~BBujlt}(k4E?");

        Email email = new Email();
        email.setToName(userName);
        email.setToEmail(emailAddress);
        email.setFromName("Bhoomitech");
        email.setFromEmail("gnsspp@bhoomitech.com");
        email.setSubject("Reset you password");
        email.setBody(body);

        return MailService.getMailService(mailConfiguration).sendMail(email);
    }

    public @NonNull ResponseEntity resetPassword(@NonNull String secret) {
        try {
            User user = validateUserSecret(secret, false);
            UserMetaData userMetaData = AuthUtil.convertJsonStringToUserMetaData(user.getUserMetaData());
            return new ResponseEntity<>(new Gson().toJson(userMetaData.getSecret()), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request " + e.getMessage());
        }
    }

    public @NonNull User validateUserSecret(String secret, boolean validateOnly) {
        try {
            String decodedValue = SecretUtil.decrypt(secret, SecretUtil.getKeyFromPassword("X)0DEL", "ledocx"));
            // check decoded value contains the secret seperator
            if (!decodedValue.contains(secretSeparator)) {
                throw new RuntimeException("Invalid secret code");
            }
            String[] splitSecret = decodedValue.split(secretSeparator);
            String userName = splitSecret[0];
            Optional<User> userOptional = userRepository.findByUsername(userName);
            // username should be exists
            if (!userOptional.isPresent()) {
                throw new RuntimeException("Invalid secret code");
            }

            User user = userOptional.get();
            // check preferences available for the user
            if (StringUtils.isEmpty(user.getUserMetaData())) {
                throw new RuntimeException("Invalid user for secret");
            }

            UserMetaData userMetaData = AuthUtil.convertJsonStringToUserMetaData(user.getUserMetaData());
            // secret code should be match for activate user
            if (!StringUtils.equals(splitSecret[1], userMetaData.getSecret().getSecret())) {
                throw new RuntimeException("Invalid user for secret");
            }
            // secret code should not be expired for secret
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (now.before(userMetaData.getSecret().getValidAfter())
                    || now.after(userMetaData.getSecret().getValidBefore())
                    || !userMetaData.getSecret().isValid()) {
                throw new RuntimeException("Your secret code is expired");
            }

            // invalidating secret code
            if (!validateOnly) {
                userMetaData.getSecret().setValid(false);
                user.setUserMetaData(AuthUtil.convertUserMetaDataToJsonString(userMetaData));
                return userRepository.save(user);
            }

            return user;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getUserByUserName(String userName) {
        return userRepository.findByUsername(userName).orElseGet(User::new);
    }
}
