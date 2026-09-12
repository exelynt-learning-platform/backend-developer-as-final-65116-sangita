package com.exelent.booking.validation;

import com.exelent.booking.exception.ApiMessages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeRangeValidator.class)
public @interface ValidTimeRange {

    String message() default ApiMessages.END_AFTER_START;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
