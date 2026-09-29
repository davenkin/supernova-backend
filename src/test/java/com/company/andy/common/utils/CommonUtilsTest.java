package com.company.andy.common.utils;

import com.company.andy.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.company.andy.common.exception.ErrorCode.SYSTEM_ERROR;
import static com.company.andy.common.utils.CommonUtils.mongoConcatFields;
import static com.company.andy.common.utils.CommonUtils.singleParameterizedArgumentClassOf;
import static org.junit.jupiter.api.Assertions.*;

class CommonUtilsTest {

    @Test
    void should_throw_exception_when_string_is_null() {
        assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.requireNonBlank(null, "string is null");
        });
    }

    @Test
    void should_throw_exception_when_string_is_empty() {
        assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.requireNonBlank("", "string is empty");
        });
    }

    @Test
    void should_throw_exception_when_string_is_blank() {
        assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.requireNonBlank("  ", "string is blank");
        });
    }

    @Test
    void should_get_single_parameterized_argument_class_for_all_level_subclass() {
        assertEquals(String.class, singleParameterizedArgumentClassOf(new FirstLevelSubClass().getClass()));
        assertEquals(String.class, singleParameterizedArgumentClassOf(new SecondLevelSubClass().getClass()));
        assertEquals(String.class, singleParameterizedArgumentClassOf(new ThirdLevelSubClass().getClass()));
    }

    @Test
    void should_throw_exception_for_two_parameterized_arguments() {
        MultiArgumentsSubClass theObject = new MultiArgumentsSubClass();
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            singleParameterizedArgumentClassOf(theObject.getClass());
        });
        assertEquals(SYSTEM_ERROR, exception.getCode());
        assertTrue(exception.toDetailMessage().contains("Expecting exactly one parameterized type argument"));
    }

    @Test
    void should_throw_exception_for_non_class_parameterized_arguments() {
        NonClassArgumentSubClass theObject = new NonClassArgumentSubClass();
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            singleParameterizedArgumentClassOf(theObject.getClass());
        });
        assertEquals(SYSTEM_ERROR, exception.getCode());
        assertTrue(exception.toDetailMessage().contains("The argument type"));
        assertTrue(exception.toDetailMessage().contains("is not of Class type"));
    }

    @Test
    void should_throw_exception_for_non_parameterized_super_class() {
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            singleParameterizedArgumentClassOf(String.class);
        });
        assertEquals(SYSTEM_ERROR, exception.getCode());
        assertTrue(exception.toDetailMessage().contains("The super class of"));
        assertTrue(exception.toDetailMessage().contains("is not of parameterized type."));
    }

    @Test
    void should_resolve_parameterized_argument_class_through_type_variable_forwarding() {
        assertEquals(String.class, singleParameterizedArgumentClassOf(new ForwardedSubClass().getClass()));
        assertEquals(String.class, singleParameterizedArgumentClassOf(new ForwardedMidSubClass().getClass()));
        assertEquals(String.class, singleParameterizedArgumentClassOf(new LastForwardedSubClass().getClass()));
    }

    @Test
    void should_concat_mongo_fields() {
        assertEquals("a.b", mongoConcatFields("a", "b"));
    }

    @Test
    void should_throw_exception_for_concat_mongo_fields_if_provided_with_null() {
        assertThrows(NullPointerException.class, () -> {
            mongoConcatFields((String[]) null);
        });
    }

    @Test
    void should_throw_exception_for_concat_mongo_fields_if_provided_with_some_null_value() {
        assertThrows(IllegalArgumentException.class, () -> {
            mongoConcatFields("a", null);
        });
    }

    @Test
    void should_throw_exception_for_concat_mongo_fields_if_provided_with_some_empty_value() {
        assertThrows(IllegalArgumentException.class, () -> {
            mongoConcatFields("a", "");
        });
    }

    @Test
    void should_throw_exception_for_concat_mongo_fields_if_provided_with_some_blank_value() {
        assertThrows(IllegalArgumentException.class, () -> {
            mongoConcatFields("a", "  ");
        });
    }

    interface SomeInterface {
    }

    interface SomeOtherInterface {
    }

    abstract class AbstractBaseClass<T> {
    }

    class FirstLevelSubClass extends AbstractBaseClass<String> implements SomeInterface {
    }

    class SecondLevelSubClass extends FirstLevelSubClass {
    }

    class ThirdLevelSubClass extends FirstLevelSubClass implements SomeOtherInterface {
    }

    class NonClassArgumentSubClass extends AbstractBaseClass<List<String>> {
    }

    abstract class AbstractMultiArgumentsBaseClass<T1, T2> {
    }

    class MultiArgumentsSubClass extends AbstractMultiArgumentsBaseClass<String, String> {
    }

    abstract class AbstractSubClass<T> extends AbstractBaseClass<T> {
    }

    class ForwardedSubClass extends AbstractSubClass<String> {
    }

    abstract class AbstractMidSubClass<T> extends AbstractSubClass<T> {
    }

    class ForwardedMidSubClass extends AbstractMidSubClass<String> {
    }

    class LastForwardedSubClass extends ForwardedSubClass {
    }
}
