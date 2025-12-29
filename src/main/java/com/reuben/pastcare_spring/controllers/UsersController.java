package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.UserCreateRequest;
import com.reuben.pastcare_spring.dtos.UserResponse;
import com.reuben.pastcare_spring.dtos.UserUpdateRequest;
import com.reuben.pastcare_spring.services.UserService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/api/users")

public class UsersController {

  private UserService userService;

  public UsersController(UserService userService) {
    this.userService = userService;
  }
    @RequirePermission(Permission.USER_VIEW)
  @GetMapping
  public List<UserResponse> getAllUsers() {
      return userService.getAllUsers();
  }

    @RequirePermission(Permission.USER_VIEW)
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
      return ResponseEntity.ok(userService.getUserById(id));
  }
  

    @RequirePermission(Permission.USER_MANAGE)
  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userRequest) {
      return ResponseEntity.ok(userService.createUser(userRequest));
  }

    @RequirePermission(Permission.USER_MANAGE)
  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userRequest) {      
      return ResponseEntity.ok(userService.updateUser(id, userRequest));
  }

    @RequirePermission(Permission.USER_MANAGE)
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
  
}
