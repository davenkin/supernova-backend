package com.company.andy.common.event.consume.external;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static lombok.AccessLevel.PROTECTED;

@Getter
@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public abstract class ExternalEvent {
    private String eventId;
    private String eventType;
}
