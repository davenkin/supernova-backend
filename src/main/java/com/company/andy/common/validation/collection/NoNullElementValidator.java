package com.company.andy.common.validation.collection;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class NoNullElementValidator implements ConstraintValidator<NoNullElement, Collection<?>> {

    @Override
    public boolean isValid(Collection collection, ConstraintValidatorContext constraintValidatorContext) {
        if (isEmpty(collection)) {
            return true;
        }

        return !collection.contains(null);
    }

}
