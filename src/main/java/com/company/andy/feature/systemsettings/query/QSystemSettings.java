package com.company.andy.feature.systemsettings.query;

import com.company.andy.feature.systemsettings.domain.BaseSettings;
import lombok.Builder;

// Query response object's name should start with "Q".

@Builder
public record QSystemSettings(
        BaseSettings baseSettings) {
}
