package com.xcodel.authservice.service;

import com.google.gson.Gson;
import com.xcodel.authservice.exception.AuthServiceException;
import com.xcodel.authservice.model.User;
import com.xcodel.authservice.model.UserDetail;
import com.xcodel.authservice.model.UserMetaData;
import com.xcodel.authservice.repository.UserDetailRepository;
import com.xcodel.authservice.repository.UserRepository;
import com.xcodel.authservice.repository.UserRoleRepository;
import com.xcodel.authservice.util.AuthUtil;
import com.xcodel.commons.auth.userdetail.UserDetailDocument;
import com.xcodel.commons.mail.MailService;
import com.xcodel.commons.mail.model.Email;
import com.xcodel.commons.mail.model.MailConfiguration;
import com.xcodel.commons.secret.SecretUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserRoleRepository userRoleRepository;
    private String domainName = "http://ec2-54-175-165-177.compute-1.amazonaws.com:12002";
    private String secretSeparator = "somethinfornow";

    @Value("${app.custom-configs.email.name}")
    private String name;

    @Value("${app.custom-configs.email.address}")
    private String emailAddress;

    @Value("${app.custom-configs.email.password}")
    private String emailPassword;

    @Value("${app.custom-configs.email.smtp-host}")
    private String smtpHost;

    @Value("${app.custom-configs.email.smtp-port}")
    private Integer smtpPort;

    @Value("${app.custom-configs.email.reset-password.subject}")
    private String resetPasswordSubject;

    @Value("${app.custom-configs.email.reset-password.body}")
    private String resetPasswordBody;

    @Value("${app.custom-configs.email.reset-password.url}")
    private String resetPasswordUrl;

    @Value("${app.custom-configs.email.activate.subject}")
    private String activateAccountSubject;

    @Value("${app.custom-configs.email.activate.body}")
    private String activateAccountBody;

    @Value("${app.custom-configs.email.activate.url}")
    private String activateAccountUrl;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserRoleRepository userRoleRepository,
                       UserDetailRepository userDetailRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
        this.userDetailRepository = userDetailRepository;
    }

    public List<UserDetailDocument> getAllUsers(String accessedUser) {
        final List<UserDetailDocument> userDetailDocuments = new ArrayList<>();
        List<User> allUsers = this.userRepository.findAll();
        allUsers.stream()
                .filter(user -> !StringUtils.equals(user.getUsername(), accessedUser))
                .forEach(user -> userDetailRepository.findByUser(user)
                        .ifPresent(userDetail -> {
                            UserDetailDocument userDetailDocument = new UserDetailDocument();
                            userDetailDocument.setId(SecretUtil.encode(String.valueOf(user.getId())));
                            userDetailDocument.setUsername(userDetail.getUser().getUsername());
                            userDetailDocument.setEmail(userDetail.getUser().getEmail());
                            userDetailDocument.setName(userDetail.getName());
                            userDetailDocument.setTelephone(userDetail.getTelephone());
                            userDetailDocument.setOrganization(userDetail.getOrganization());
                            userDetailDocument.setStatus(userDetail.getUser().getEnabled() ? "ACTIVE" : "INACTIVE");
                            userDetailDocuments.add(userDetailDocument);
                        }));
        return userDetailDocuments;
    }

    public void createNewUser(UserDetailDocument userDetailDocument) {
        User user = new User();
        user.setUsername(userDetailDocument.getUsername());
        user.setPassword(passwordEncoder.encode(userDetailDocument.getPassword()));
        user.setEmail(userDetailDocument.getEmail());
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
        sendActivateUserMail(user);
    }

    public UserDetailDocument getUserById(Integer userId) {
        UserDetailDocument userDetailDocument = new UserDetailDocument();
        Optional<User> userOptional = this.userRepository.findById(userId);

        userOptional.ifPresent(user -> {
            Optional<UserDetail> userDetailOptional = this.userDetailRepository.findByUser(user);
            userDetailOptional.ifPresent(userDetail -> {
                userDetailDocument.setId(SecretUtil.encode(String.valueOf(userDetail.getUser().getId())));
                userDetailDocument.setUsername(userDetail.getUser().getUsername());
                userDetailDocument.setEmail(userDetail.getUser().getEmail());
                userDetailDocument.setName(userDetail.getName());
                userDetailDocument.setTelephone(userDetail.getTelephone());
                userDetailDocument.setOrganization(userDetail.getOrganization());
                userDetailDocument.setStatus(userDetail.getUser().getEnabled() ? "ACTIVE" : "INACTIVE");
            });
        });
        return userDetailDocument;
    }

    public void activateUser(String encryptedUserID) {
        User user = getUserFromEncryptedId(encryptedUserID);
        user.setEnabled(Boolean.TRUE);
        userRepository.save(user);
    }

    public void deactivateUser(String encryptedUserID) {
        User user = getUserFromEncryptedId(encryptedUserID);
        user.setEnabled(Boolean.FALSE);
        userRepository.save(user);
    }

    private User getUserFromEncryptedId(String encryptedUserID) {
        try {
            String userId = SecretUtil.decode(encryptedUserID);
            Optional<User> optionalUser = userRepository.findById(Integer.parseInt(userId));
            if (optionalUser.isPresent()) {
                return optionalUser.get();
            }
            throw new AuthServiceException("Invalid user");
        } catch (Exception e) {
            throw new AuthServiceException("Sorry we cant process your request");
        }
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
            mailBody.append(resetPasswordBody.replace("{user_name}", user.getUsername()))
                    .append("\n")
                    .append(resetPasswordUrl.replace("{token}", resetPwSecret));
            sendMail(user.getUsername(), user.getEmail(), mailBody.toString(), resetPasswordSubject);
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
            mailBody.append(activateAccountBody.replace("{user_name}", user.getUsername()))
                    .append("\n")
                    .append(activateAccountUrl.replace("{token}", resetPwSecret));
            sendMail(user.getUsername(), user.getEmail(), mailBody.toString(), "Activate your account");
        } catch (Exception e) {
            log.error("error occurred while performing forgot password", e);
            throw new AuthServiceException("Sorry, something went wrong", e);
        }
    }

    private Boolean sendMail(@NonNull String userName, @NonNull String toEmail, @NonNull String body, String subject) {
        MailConfiguration mailConfiguration = new MailConfiguration();
        mailConfiguration.setSmtpHost(smtpHost);
        mailConfiguration.setSmtpPort(smtpPort);
        mailConfiguration.setSmtpUserName(emailAddress);
        mailConfiguration.setSmtpPassword(emailPassword);

        Email email = new Email();
        email.setToName(userName);
        email.setToEmail(toEmail);
        email.setFromName(name);
        email.setFromEmail(emailAddress);
        email.setSubject(subject);
        email.setBody(body);

        boolean success = MailService.getMailService(mailConfiguration).sendMail(email);
        if (success)
            log.info("email sent success to {}", toEmail);
        else
            log.error("email sent failed to {}", toEmail);
        return success;
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
            String decodedValue = SecretUtil.decode(secret);
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
        isValidPassword(userDetailDocument.getPassword());
        // validate email exists
        userRepository.findByEmail(userDetailDocument.getEmail())
                .ifPresent(u -> {
                    throw new AuthServiceException("This email already exists");
                });
        // validate username exists
        userRepository.findByUsername(userDetailDocument.getUsername())
                .ifPresent(u -> {
                    throw new AuthServiceException("This username already taken");
                });


    }

    public void updatePassword(User user, String password, String confirmPassword) {
        if (StringUtils.isEmpty(password)) {
            throw new AuthServiceException("Invalid password");
        }
        if (!StringUtils.equals(password, confirmPassword)) {
            throw new AuthServiceException("password does not match");
        }
        isValidPassword(password);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("USER: updated the password {}", user);
    }

    private void isValidPassword(@NonNull String password) {
        if (password.length() < 8) {
            throw new AuthServiceException("password should have at least 8 characters");
        }
    }
}
