package com.reuben.pastcare_spring.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reuben.pastcare_spring.dtos.UserDto;
import com.reuben.pastcare_spring.mapper.UserMapper;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.requests.UserCreateRequest;
import com.reuben.pastcare_spring.requests.UserUpdateRequest;
import com.reuben.pastcare_spring.respositories.ChapelRepository;
import com.reuben.pastcare_spring.respositories.UserRepository;

@Service
public class UserService {

  private UserRepository userRepository;
  private ChapelRepository chapelRepository;

  public UserService(UserRepository userRepository, ChapelRepository chapelRepository){
    this.userRepository = userRepository;
    this.chapelRepository = chapelRepository;
  }
  public List<UserDto> getAllUsers()
  {
    return userRepository.findAll()
      .stream()
      .map(UserMapper::toDto)
      .toList();
  }

  public UserDto getUserById(Integer id){
    var user = userRepository.findById(id).orElse(new User());
    return UserMapper.toDto(user);
  }

  public UserDto createUser(UserCreateRequest userRequest){
    User user = new User();
    user.setName(userRequest.name());
    user.setEmail(userRequest.email());
    user.setPhoneNumber(userRequest.phoneNumber());
    user.setTitle(userRequest.title());
    user.setPrimaryService(userRequest.primaryService());
    user.setDesignation(userRequest.designation());
    user.setPassword(userRequest.password());

    var chapel = chapelRepository.findById(userRequest.chapelId())
      .orElseThrow(() -> new IllegalArgumentException("Chapel not found"));

    user.setChapel(chapel);

    userRepository.save(user);

    return UserMapper.toDto(user);
  }

  public UserDto updateUser(Integer id, UserUpdateRequest userRequest){
    User user = userRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));
    user.setName(userRequest.name());
    user.setEmail(userRequest.email());
    user.setPhoneNumber(userRequest.phoneNumber());
    user.setTitle(userRequest.title());
    user.setPrimaryService(userRequest.primaryService());
    user.setDesignation(userRequest.designation());

    var chapel = chapelRepository.findById(userRequest.chapelId())
      .orElseThrow(() -> new IllegalArgumentException("Chapel not found"));
    if (userRequest.chapelId() != null) {
        user.setChapel(chapel);
    }

    User savedUser = userRepository.save(user);

    return UserMapper.toDto(savedUser);
  }

  public void deleteUser(Integer id){
    User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    userRepository.delete(user);
  }
}
