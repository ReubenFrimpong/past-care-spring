package com.reuben.pastcare_spring.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.reuben.pastcare_spring.dtos.UserCreateRequest;
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

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FellowshipRepository fellowshipRepository;

  @Autowired
  private ChurchRepository churchRepository;


  public List<UserResponse> getAllUsers()
  {
    return userRepository.findAll()
      .stream()
      .map(UserMapper::toUserResponse)
      .toList();
  }

  public UserResponse getUserById(Long id){
    var user = userRepository.findById(id).orElse(new User());
    return UserMapper.toUserResponse(user);
  }

  public UserResponse createUser(UserCreateRequest userRequest){
    User user = new User();
    user.setName(userRequest.name());
    user.setEmail(userRequest.email());
    user.setPhoneNumber(userRequest.phoneNumber());
    user.setTitle(userRequest.title());
    user.setPrimaryService(userRequest.primaryService());
    user.setRole(userRequest.role());

    if(userRequest.churchId() != null){
      Church church = churchRepository.findById(userRequest.churchId())
          .orElseThrow(() -> new IllegalArgumentException("Invalid church ID provided"));
      user.setChurch(church);
    }

    List<Fellowship> fellowships = fellowshipRepository.findAllById(userRequest.fellowshipIds());

    if (fellowships.isEmpty()) {
        throw new IllegalArgumentException("Invalid fellowship IDs provided");
    }

    user.setFellowships(fellowships);

    userRepository.save(user);

    return UserMapper.toUserResponse(user);
  }

  public UserResponse updateUser(Long id, UserUpdateRequest userRequest){
    User user = userRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));
    user.setName(userRequest.name());
    user.setEmail(userRequest.email());
    user.setPhoneNumber(userRequest.phoneNumber());
    user.setTitle(userRequest.title());
    user.setPrimaryService(userRequest.primaryService());
    user.setRole(userRequest.role());

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
    String currentRole = principal.getRole().name();

    if (!currentRole.equals("SUPERADMIN") && userRequest.churchId() == null) {
        throw new IllegalArgumentException("churchId is required");
    }

    if(userRequest.churchId() != null){
      Church church = churchRepository.findById(userRequest.churchId())
          .orElseThrow(() -> new IllegalArgumentException("Invalid church ID provided"));
      user.setChurch(church);
    }
    List<Fellowship> fellowships = fellowshipRepository.findAllById(userRequest.fellowshipIds());

    if (fellowships.isEmpty()) {
        throw new IllegalArgumentException("Invalid fellowship IDs provided");
    }

    user.setFellowships(fellowships);

    User savedUser = userRepository.save(user);

    return UserMapper.toUserResponse(savedUser);
  }

  public void deleteUser(Long id){
    User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    userRepository.delete(user);
  }
}
