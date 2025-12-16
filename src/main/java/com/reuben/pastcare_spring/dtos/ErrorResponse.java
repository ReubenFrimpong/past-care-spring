package com.reuben.pastcare_spring.dtos;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ErrorResponse(
  int status,
  String error,
  String message,
  String path,
  Instant timestamp,
  Map<String, List<String>> validationErrors
) {
  public ErrorResponse(int status, String error, String message, String path) {
    this(status, error, message, path, Instant.now(), null);
  }

  public ErrorResponse(int status, String error, String message, String path, Map<String, List<String>> validationErrors) {
    this(status, error, message, path, Instant.now(), validationErrors);
  }
}
