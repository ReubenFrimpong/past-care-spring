package com.reuben.pastcare_spring.validators;

import com.reuben.pastcare_spring.annotations.Unique;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueValidator implements ConstraintValidator<Unique, Object> {

  private String table;
  private String column;

  @Override
  public void initialize(Unique constraintAnnotation) {
    this.table = constraintAnnotation.table();
    this.column = constraintAnnotation.column();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    return true;
  }
}
