package com.reuben.pastcare_spring.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.reuben.pastcare_spring.dtos.AuthRegistrationRequest;
import com.reuben.pastcare_spring.dtos.AuthResponse;
import com.reuben.pastcare_spring.dtos.UserResponse;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.security.JwtUtil;



@Service
public class AuthService {
  @Autowired
  UserRepository userRepository;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private JwtUtil jwtUtil;


  public AuthResponse login(AuthRegistrationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    final UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
    String token = jwtUtil.generateToken(userDetails);
    User user = userRepository.findByEmail(request.email()).orElse(null);
    return new AuthResponse(token, new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber(), user.getTitle(), user.getChurch(), user.getFellowships(), user.getPrimaryService(), user.getRole()));
  }

  public User register(AuthRegistrationRequest authRequest) {
    User user = new User();
    user.setName(authRequest.name());
    user.setEmail(authRequest.email());
    user.setPassword(authRequest.password());
    user.setRole(authRequest.role());
    user.setPhoneNumber(authRequest.phoneNumber());
    user.setPassword(new BCryptPasswordEncoder().encode(authRequest.password()));
    return userRepository.save(user);
  }
}
