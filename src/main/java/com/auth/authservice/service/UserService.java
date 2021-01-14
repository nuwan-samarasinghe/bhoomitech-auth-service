package com.auth.authservice.service;

import com.auth.authservice.exception.AuthServiceException;
import com.auth.authservice.model.User;
import com.auth.authservice.model.UserDetail;
import com.auth.authservice.model.UserMetaData;
import com.auth.authservice.repository.UserDetailRepository;
import com.auth.authservice.repository.UserRepository;
import com.auth.authservice.repository.UserRoleRepository;
import com.auth.authservice.util.AuthUtil;
import com.google.gson.Gson;
import com.xcodel.auth.lib.userdetail.UserDetailDocument;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserRoleRepository userRoleRepository;
    private String domainName = "http://localhost:12002";
    private String secretSeparator = "somethinfornow";

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserRoleRepository userRoleRepository,
                       UserDetailRepository userDetailRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
        this.userDetailRepository = userDetailRepository;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public void createNewUser(UserDetailDocument userDetailDocument) {
        User user = new User();
        user.setUsername(userDetailDocument.getUserName());
        user.setPassword(passwordEncoder.encode(userDetailDocument.getPassword()));
        user.setEnabled(Boolean.FALSE);
        user.setAccountNonExpired(Boolean.TRUE);
        user.setAccountNonLocked(Boolean.TRUE);
        user.setCredentialsNonExpired(Boolean.TRUE);
        user.setRoles(this.userRoleRepository.findAllById(Collections.singleton(2)));
        User savedUser = this.userRepository.save(user);
        log.info("USER : creation successful {}", savedUser);

        // creating user details
        UserDetail userDetail = new UserDetail();
        userDetail.setUser(savedUser);
        userDetail.setName(userDetailDocument.getName());
        userDetail.setAddress(userDetailDocument.getAddress());
        userDetail.setOrganization(userDetailDocument.getOrganization());
        userDetail.setTelephone(userDetailDocument.getTelephone());
        userDetail.setEmail(userDetailDocument.getEmail());
        UserDetail savedDetail = this.userDetailRepository.save(userDetail);
        log.info("USER DETAIL : creation successful {}", savedDetail);

        // sending mail since new user created
        sendActivateUserMail(savedUser);
    }

    public User getUserById(Integer userId) {
        Optional<User> userOptional = this.userRepository.findById(userId);
        return userOptional.orElse(null);
    }

    public boolean forgotPassword(@Nullable String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            sendForgotPasswordMail(user);
        } else {
            throw new AuthServiceException("Sorry, We couldn't find your account.");
        }
        return true;
    }

    private void sendForgotPasswordMail(User user) {
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
                    .append("please click the following link to activate your account")
                    .append("\n")
                    .append(String.format("%s/activate?token=", domainName))
                    .append(resetPwSecret);
            sendMail(user.getUsername(), user.getEmail(), mailBody.toString(), "Activate your account");
        } catch (Exception e) {
            log.error("error occurred while performing forgot password", e);
            throw new AuthServiceException("Sorry, something went wrong", e);
        }
    }

    private void sendActivateUserMail(User user) {
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
                    .append(String.format("%s/reset-password?token=", domainName))
                    .append(resetPwSecret);
            sendMail(user.getUsername(), user.getEmail(), mailBody.toString(), "Reset you password");
        } catch (Exception e) {
            log.error("error occurred while performing forgot password", e);
            throw new AuthServiceException("Sorry, something went wrong", e);
        }
    }

    private Boolean sendMail(@NonNull String userName, @NonNull String emailAddress, @NonNull String body, String subject) {
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
        email.setSubject(subject);
        email.setBody(body);

        return MailService.getMailService(mailConfiguration).sendMail(email);
    }

    public @NonNull ResponseEntity resetPassword(@NonNull String secret) {
        try {
            User user = validateUserSecret(secret, false);
            UserMetaData userMetaData = AuthUtil.convertJsonStringToUserMetaData(user.getUserMetaData());
            return new ResponseEntity<>(new Gson().toJson(userMetaData.getSecret()), HttpStatus.OK);
        } catch (Exception e) {
            throw new AuthServiceException("");
        }
    }

    public @NonNull User validateUserSecret(String secret, boolean validateOnly) {
        try {
            String decodedValue = SecretUtil.decrypt(secret, SecretUtil.getKeyFromPassword("X)0DEL", "ledocx"));
            // check decoded value contains the secret seperator
            if (!decodedValue.contains(secretSeparator)) {
                throw new AuthServiceException("Invalid secret code");
            }
            String[] splitSecret = decodedValue.split(secretSeparator);
            String userName = splitSecret[0];
            Optional<User> userOptional = userRepository.findByUsername(userName);
            // username should be exists
            if (!userOptional.isPresent()) {
                throw new AuthServiceException("Invalid secret code");
            }

            User user = userOptional.get();
            // check preferences available for the user
            if (StringUtils.isEmpty(user.getUserMetaData())) {
                throw new AuthServiceException("Invalid user for secret");
            }

            UserMetaData userMetaData = AuthUtil.convertJsonStringToUserMetaData(user.getUserMetaData());
            // secret code should be match for activate user
            if (!StringUtils.equals(splitSecret[1], userMetaData.getSecret().getSecret())) {
                throw new AuthServiceException("Invalid user for secret");
            }
            // secret code should not be expired for secret
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (now.before(userMetaData.getSecret().getValidAfter())
                    || now.after(userMetaData.getSecret().getValidBefore())
                    || !userMetaData.getSecret().isValid()) {
                throw new AuthServiceException("Your secret code is expired");
            }

            // invalidating secret code
            if (!validateOnly) {
                userMetaData.getSecret().setValid(false);
                user.setUserMetaData(AuthUtil.convertUserMetaDataToJsonString(userMetaData));
                user.setEnabled(Boolean.TRUE);
                return userRepository.save(user);
            }
            return user;
        } catch (Exception e) {
            log.error("error on decrypting forgot password token", e);
            throw new AuthServiceException("Sorry, something went wrong");
        }
    }

    public User getUserByUserName(String userName) {
        return userRepository.findByUsername(userName).orElseGet(User::new);
    }

    public void validateNewUser(UserDetailDocument userDetailDocument) {
        if (Objects.isNull(userDetailDocument)) {
            throw new AuthServiceException("Insufficient data to process.");
        }
        if (StringUtils.isEmpty(userDetailDocument.getName())) {
            throw new AuthServiceException("Name cannot be empty");
        }
        if (StringUtils.isEmpty(userDetailDocument.getEmail())) {
            throw new AuthServiceException("Email cannot be empty");
        }
        if (StringUtils.isEmpty(userDetailDocument.getPassword())) {
            throw new AuthServiceException("Password cannot be empty");
        }
        if (!StringUtils.equals(userDetailDocument.getPassword(), userDetailDocument.getConfirmPassword())) {
            throw new AuthServiceException("Password does not match");
        }
    }

    public void updatePassword(User user, String password, String confirmPassword) {
        if (StringUtils.isEmpty(password)) {
            throw new AuthServiceException("Invalid password");
        }
        if (!StringUtils.equals(password, confirmPassword)) {
            throw new AuthServiceException("password does not match");
        }
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("USER: updated the password {}", user);
    }
}
