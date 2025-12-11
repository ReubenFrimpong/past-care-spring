package com.reuben.pastcare_spring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.reuben.pastcare_spring.validators.UniqueValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValidator.class)

public @interface Unique {
  String message() default "Field must be unique";

  String table();

  String column();
  
  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
