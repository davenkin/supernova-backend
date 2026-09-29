package com.company.andy.common.validation.mobilenumber;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.company.andy.common.utils.Constants.MOBILE_NUMBER_REGEX;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MobileNumberValidator implements ConstraintValidator<MobileNumber, String> {
    private static final Pattern MOBILE_NUMBER_PATTERN = Pattern.compile(MOBILE_NUMBER_REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isBlank(value)) {
            return true;
        }

        return MOBILE_NUMBER_PATTERN.matcher(value).matches();
    }
}
