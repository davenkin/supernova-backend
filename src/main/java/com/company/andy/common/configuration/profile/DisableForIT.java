package com.company.andy.common.configuration.profile;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

// Disable for integration tests, covering both "it-embedded" and "it-local" profiles
@Retention(RUNTIME)
@Profile("!(it-embedded | it-local)")
public @interface DisableForIT {
}
