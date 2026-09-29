package com.company.andy.feature.systemsettings.domain;

import com.company.andy.common.validation.collection.NoBlankString;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record BaseSettings(
        @Valid
        @NotNull
        @NoBlankString
        @Size(max = 100)
        List<@NotBlank String> demoReservationNotificationEmails) {
}
