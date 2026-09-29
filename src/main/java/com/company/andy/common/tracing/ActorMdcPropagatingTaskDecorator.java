package com.company.andy.common.tracing;

import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import static com.company.andy.common.tracing.ActorMdcSupport.ACTOR_MDC_KEYS;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@NullMarked
public class ActorMdcPropagatingTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        return () -> {
            ACTOR_MDC_KEYS.stream()
                    .filter(key -> isNotBlank(MDC.get(key)))
                    .collect(toMap(key -> key, MDC::get))
                    .forEach(MDC::put);
            try {
                runnable.run();
            } finally {
                ACTOR_MDC_KEYS.forEach(MDC::remove);
            }
        };
    }
}
