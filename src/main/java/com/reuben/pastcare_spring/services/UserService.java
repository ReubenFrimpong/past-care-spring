package com.reuben.pastcare_spring.services;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reuben.pastcare_spring.dtos.UserCreateRequest;
import com.reuben.pastcare_spring.dtos.UserCreateResponse;
import com.reuben.pastcare_spring.dtos.UserResponse;
import com.reuben.pastcare_spring.dtos.UserUpdateRequest;
import com.reuben.pastcare_spring.mapper.UserMapper;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.FellowshipRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.security.UserPrincipal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Service
@Slf4j
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FellowshipRepository fellowshipRepository;

  @Autowired
  private ChurchRepository churchRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private EmailService emailService;

  @Autowired
  private EmailTemplateService emailTemplateService;

  @Value("${app.url:http://localhost:4200}")
  private String appUrl;

  @Value("${app.email.send-credentials:true}")
  private boolean sendCredentialsViaEmail;

  private static final String CHAR_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String CHAR_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String CHAR_DIGITS = "0123456789";
  private static final String CHAR_SPECIAL = "!@#$%^&*";
  private static final String PASSWORD_CHARS = CHAR_LOWERCASE + CHAR_UPPERCASE + CHAR_DIGITS + CHAR_SPECIAL;
  private static final SecureRandom random = new SecureRandom();

  /**
   * Generates a secure random password
   * @param length the length of the password to generate
   * @return a randomly generated password
   */
  private String generateSecurePassword(int length) {
    if (length < 8) {
      throw new IllegalArgumentException("Password length must be at least 8 characters");
    }

    StringBuilder password = new StringBuilder(length);

    // Ensure at least one character from each category
    password.append(CHAR_LOWERCASE.charAt(random.nextInt(CHAR_LOWERCASE.length())));
    password.append(CHAR_UPPERCASE.charAt(random.nextInt(CHAR_UPPERCASE.length())));
    password.append(CHAR_DIGITS.charAt(random.nextInt(CHAR_DIGITS.length())));
    password.append(CHAR_SPECIAL.charAt(random.nextInt(CHAR_SPECIAL.length())));

    // Fill the rest with random characters
    for (int i = 4; i < length; i++) {
      password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
    }

    // Shuffle the password characters
    char[] passwordArray = password.toString().toCharArray();
    for (int i = passwordArray.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      char temp = passwordArray[i];
      passwordArray[i] = passwordArray[j];
      passwordArray[j] = temp;
    }

    return new String(passwordArray);
  }


  public List<UserResponse> getAllUsers()
  {
    // Get the current authenticated user's church
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    // SUPERADMIN can see all users, others only see users from their church
    if ("SUPERADMIN".equals(principal.getRole().name())) {
      return userRepository.findAll()
        .stream()
        .map(UserMapper::toUserResponse)
        .toList();
    } else {
      Long churchId = principal.getChurchId();
      return userRepository.findAll()
        .stream()
        .filter(user -> user.getChurch() != null && user.getChurch().getId().equals(churchId))
        .map(UserMapper::toUserResponse)
        .toList();
    }
  }

  public UserResponse getUserById(Long id){
    var user = userRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Check if user has access to this user (same church or SUPERADMIN)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    if (!"SUPERADMIN".equals(principal.getRole().name())) {
      Long churchId = principal.getChurchId();
      if (user.getChurch() == null || !user.getChurch().getId().equals(churchId)) {
        throw new IllegalArgumentException("Access denied: User belongs to different church");
      }
    }

    return UserMapper.toUserResponse(user);
  }

  public UserCreateResponse createUser(UserCreateRequest userRequest){
    // Get the current authenticated user
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    User user = new User();
    user.setName(userRequest.name());
    user.setEmail(userRequest.email());
    user.setPhoneNumber(userRequest.phoneNumber());
    user.setTitle(userRequest.title());
    user.setRole(userRequest.role());

    // Generate or use provided password
    String temporaryPassword;
    if (userRequest.password() != null && !userRequest.password().trim().isEmpty()) {
      // Use provided password
      temporaryPassword = userRequest.password();
    } else {
      // Auto-generate a secure password
      temporaryPassword = generateSecurePassword(12);
    }

    // Encode and set the password
    user.setPassword(passwordEncoder.encode(temporaryPassword));

    // Set must change password flag for new users
    user.setMustChangePassword(true);

    // Set user as active by default
    user.setActive(true);

    // Set church: SUPERADMIN can specify any church, others must use their own church
    if ("SUPERADMIN".equals(principal.getRole().name())) {
      if(userRequest.churchId() != null){
        Church church = churchRepository.findById(userRequest.churchId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid church ID provided"));
        user.setChurch(church);
      }
    } else {
      // Non-SUPERADMIN users can only create users for their own church
      Long churchId = principal.getChurchId();
      if (churchId == null) {
        throw new IllegalArgumentException("Current user is not associated with a church");
      }
      Church church = churchRepository.findById(churchId)
          .orElseThrow(() -> new IllegalArgumentException("Church not found"));
      user.setChurch(church);
    }

    // Only set fellowships if IDs are provided
    if (userRequest.fellowshipIds() != null && !userRequest.fellowshipIds().isEmpty()) {
      List<Fellowship> fellowships = fellowshipRepository.findAllById(userRequest.fellowshipIds());
      if (!fellowships.isEmpty()) {
        user.setFellowships(fellowships);
      }
    }

    User savedUser = userRepository.save(user);

    // Send email notification with credentials if enabled
    String responseMessage;
    if (sendCredentialsViaEmail && emailService.isEmailEnabled()) {
      try {
        sendWelcomeEmail(savedUser, temporaryPassword);
        responseMessage = "User created successfully. Welcome email with login credentials has been sent to " + savedUser.getEmail();
        log.info("✅ User created and welcome email sent to: {}", savedUser.getEmail());
      } catch (Exception e) {
        log.error("❌ Failed to send welcome email to: {}. User created but email failed.", savedUser.getEmail(), e);
        responseMessage = "User created successfully, but failed to send email. Please share this password manually: " + temporaryPassword;
      }
    } else {
      responseMessage = "User created successfully. Email notifications are disabled. Please share the temporary password with the user and ask them to change it on first login.";
      log.info("✅ User created. Email disabled - admin must share password manually for: {}", savedUser.getEmail());
    }

    // Return response with temporary password
    return new UserCreateResponse(
      savedUser.getId(),
      savedUser.getName(),
      savedUser.getEmail(),
      savedUser.getPhoneNumber(),
      savedUser.getTitle(),
      savedUser.getChurch(),
      savedUser.getFellowships(),
      savedUser.getRole(),
      temporaryPassword,  // Include the plain-text password for the admin to share (as backup)
      responseMessage
    );
  }

  /**
   * Send welcome email to new user with login credentials
   */
  private void sendWelcomeEmail(User user, String temporaryPassword) {
    String churchName = user.getChurch() != null ? user.getChurch().getName() : "Your Organization";
    String loginUrl = appUrl + "/login";

    // Generate HTML email
    String htmlBody = emailTemplateService.generateNewUserCredentialsEmail(
      user.getName(),
      user.getEmail(),
      temporaryPassword,
      churchName,
      loginUrl
    );

    // Generate plain text fallback
    String textBody = emailTemplateService.generateNewUserCredentialsTextEmail(
      user.getName(),
      user.getEmail(),
      temporaryPassword,
      churchName,
      loginUrl
    );

    // Send email
    emailService.sendHtmlEmail(
      user.getEmail(),
      "Welcome to PastCare - Your Account Credentials",
      htmlBody,
      textBody
    );
  }

  public UserResponse updateUser(Long id, UserUpdateRequest userRequest){
    User user = userRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Check if user has access to update this user (same church or SUPERADMIN)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    if (!"SUPERADMIN".equals(principal.getRole().name())) {
      Long churchId = principal.getChurchId();
      if (user.getChurch() == null || !user.getChurch().getId().equals(churchId)) {
        throw new IllegalArgumentException("Access denied: Cannot update user from different church");
      }
    }

    user.setName(userRequest.name());
    user.setEmail(userRequest.email());
    user.setPhoneNumber(userRequest.phoneNumber());
    user.setTitle(userRequest.title());
    user.setRole(userRequest.role());

    // Only SUPERADMIN can change church association
    if ("SUPERADMIN".equals(principal.getRole().name())) {
      if(userRequest.churchId() != null){
        Church church = churchRepository.findById(userRequest.churchId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid church ID provided"));
        user.setChurch(church);
      }
    }
    // Non-SUPERADMIN cannot change church - it stays the same

    // Only update fellowships if IDs are provided
    if (userRequest.fellowshipIds() != null && !userRequest.fellowshipIds().isEmpty()) {
      List<Fellowship> fellowships = fellowshipRepository.findAllById(userRequest.fellowshipIds());
      if (!fellowships.isEmpty()) {
        user.setFellowships(fellowships);
      }
    }

    User savedUser = userRepository.save(user);

    return UserMapper.toUserResponse(savedUser);
  }

  public void deleteUser(Long id){
    User user = userRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Check if user has access to delete this user (same church or SUPERADMIN)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    if (!"SUPERADMIN".equals(principal.getRole().name())) {
      Long churchId = principal.getChurchId();
      if (user.getChurch() == null || !user.getChurch().getId().equals(churchId)) {
        throw new IllegalArgumentException("Access denied: Cannot delete user from different church");
      }
    }

    userRepository.delete(user);
  }

  /**
   * Deactivate a user (soft delete)
   * @param id the user ID to deactivate
   */
  public void deactivateUser(Long id) {
    var user = userRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Check if user has access to deactivate this user (same church or SUPERADMIN)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    if (!"SUPERADMIN".equals(principal.getRole().name())) {
      Long churchId = principal.getChurchId();
      if (user.getChurch() == null || !user.getChurch().getId().equals(churchId)) {
        throw new IllegalArgumentException("Access denied: Cannot deactivate user from different church");
      }
    }

    user.setActive(false);
    userRepository.save(user);
    log.info("✅ User deactivated: {} (ID: {})", user.getEmail(), id);
  }

  /**
   * Reactivate a deactivated user
   * @param id the user ID to reactivate
   */
  public void reactivateUser(Long id) {
    var user = userRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Check if user has access to reactivate this user (same church or SUPERADMIN)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    if (!"SUPERADMIN".equals(principal.getRole().name())) {
      Long churchId = principal.getChurchId();
      if (user.getChurch() == null || !user.getChurch().getId().equals(churchId)) {
        throw new IllegalArgumentException("Access denied: Cannot reactivate user from different church");
      }
    }

    user.setActive(true);
    userRepository.save(user);
    log.info("✅ User reactivated: {} (ID: {})", user.getEmail(), id);
  }

  /**
   * Update user's last login timestamp
   * This should be called by the authentication service on successful login
   * @param userId the user ID
   */
  public void updateLastLogin(Long userId) {
    var user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user.setLastLoginAt(java.time.LocalDateTime.now());
    userRepository.save(user);
    log.debug("Updated last login for user: {} at {}", user.getEmail(), user.getLastLoginAt());
  }

  /**
   * Get all active users (excludes deactivated)
   */
  public List<UserResponse> getActiveUsers() {
    // Get the current authenticated user's church
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    // SUPERADMIN can see all users, others only see users from their church
    if ("SUPERADMIN".equals(principal.getRole().name())) {
      return userRepository.findAll()
        .stream()
        .filter(User::isActive)
        .map(UserMapper::toUserResponse)
        .toList();
    } else {
      Long churchId = principal.getChurchId();
      return userRepository.findAll()
        .stream()
        .filter(user -> user.getChurch() != null && user.getChurch().getId().equals(churchId))
        .filter(User::isActive)
        .map(UserMapper::toUserResponse)
        .toList();
    }
  }

  /**
   * Force reset a user's password to a temporary password (SUPERADMIN only)
   * User must change password on next login
   * @param userId the user ID to reset password for
   * @return the temporary password
   */
  public String forceResetPassword(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Only SUPERADMIN can force reset passwords (defense in depth check)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    if (!"SUPERADMIN".equals(principal.getRole().name())) {
      throw new SecurityException("Access denied: Only SUPERADMIN can force reset passwords");
    }

    // Generate a secure temporary password
    String temporaryPassword = generateSecurePassword(12);

    // Set the new password and force change on next login
    user.setPassword(passwordEncoder.encode(temporaryPassword));
    user.setMustChangePassword(true);
    userRepository.save(user);

    // Send email notification if enabled
    if (sendCredentialsViaEmail && emailService.isEmailEnabled()) {
      try {
        sendPasswordResetEmail(user, temporaryPassword);
        log.info("✅ Password reset and email sent to: {}", user.getEmail());
      } catch (Exception e) {
        log.error("❌ Failed to send password reset email to: {}. Password reset but email failed.", user.getEmail(), e);
      }
    } else {
      log.info("✅ Password reset for user: {}. Email disabled - admin must share password manually.", user.getEmail());
    }

    return temporaryPassword;
  }

  /**
   * Send password reset email to user with temporary password
   */
  private void sendPasswordResetEmail(User user, String temporaryPassword) {
    String churchName = user.getChurch() != null ? user.getChurch().getName() : "Your Organization";
    String loginUrl = appUrl + "/login";

    // Generate HTML email
    String htmlBody = emailTemplateService.generatePasswordResetByAdminEmail(
      user.getName(),
      user.getEmail(),
      temporaryPassword,
      churchName,
      loginUrl
    );

    // Generate plain text fallback
    String textBody = emailTemplateService.generatePasswordResetByAdminTextEmail(
      user.getName(),
      user.getEmail(),
      temporaryPassword,
      churchName,
      loginUrl
    );

    // Send email
    emailService.sendHtmlEmail(
      user.getEmail(),
      "Your PastCare Password Has Been Reset",
      htmlBody,
      textBody
    );
  }
}
