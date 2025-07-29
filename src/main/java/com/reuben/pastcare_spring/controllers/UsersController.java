package com.reuben.pastcare_spring.controllers;

import java.util.HashMap;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.Dtos.UserDto;
import com.reuben.pastcare_spring.requests.UserRequest;
import com.reuben.pastcare_spring.services.UserService;

import jakarta.validation.Valid;



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

  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequest userRequest) {
      UserDto createdUser = userService.createUser(userRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
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
}
