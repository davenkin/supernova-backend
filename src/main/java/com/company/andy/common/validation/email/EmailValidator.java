package com.company.andy.common.validation.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.company.andy.common.utils.Constants.EMAIL_REGEX;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class EmailValidator implements ConstraintValidator<Email, String> {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isBlank(value)) {
            return true;
        }

        return value.length() <= 100 && EMAIL_PATTERN.matcher(value).matches();
    }
}
