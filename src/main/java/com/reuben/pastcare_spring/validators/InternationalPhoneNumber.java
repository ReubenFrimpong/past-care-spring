package com.reuben.pastcare_spring.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = InternationalPhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface InternationalPhoneNumber {
    String message() default "Phone number must be a valid international phone number (e.g., +1234567890 or 1234567890)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
