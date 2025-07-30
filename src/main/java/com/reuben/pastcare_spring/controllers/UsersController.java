package com.reuben.pastcare_spring.controllers;

import java.util.HashMap;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.UserDto;
import com.reuben.pastcare_spring.requests.UserCreateRequest;
import com.reuben.pastcare_spring.requests.UserUpdateRequest;
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
  @GetMapping
  public List<UserDto> getAllUsers() {
      return userService.getAllUsers();
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
      var userDto = userService.getUserById(id);
      return ResponseEntity.status(HttpStatus.OK).body(userDto);
  }
  

  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest userRequest) {
      UserDto createdUser = userService.createUser(userRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(@PathVariable Integer id, @Valid @RequestBody UserUpdateRequest userRequest) {
      UserDto updatedUser = userService.updateUser(id, userRequest);
      
      return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
  
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exp) {
    var errors = new HashMap<String, String>();
    exp.getBindingResult().getAllErrors().forEach(error -> {
      var fieldName = ((FieldError) error).getField();
      var errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException exp) {
    var error = new HashMap<String, String>();
    error.put("error", exp.getLocalizedMessage());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
}
