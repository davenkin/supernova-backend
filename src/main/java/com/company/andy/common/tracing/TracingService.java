package com.company.andy.common.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static com.company.andy.common.utils.Constants.TRACE_PARENT;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@RequiredArgsConstructor
public class TracingService {
    private final ObjectProvider<Tracer> tracerProvider;
    private final ObjectProvider<Propagator> propagatorProvider;

    public String currentTraceId() {
        Tracer tracer = this.tracerProvider.getIfAvailable();
        if (tracer == null) {
            return null;
        }

        TraceContext context = tracer.currentTraceContext().context();
        return context != null ? context.traceId() : null;
    }

    public String currentTraceParent() {
        Tracer tracer = this.tracerProvider.getIfAvailable();
        Propagator propagator = this.propagatorProvider.getIfAvailable();

        if (tracer == null || propagator == null) {
            return null;
        }

        Map<String, String> carrier = new HashMap<>();
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            propagator.inject(currentSpan.context(), carrier, Map::put);
        }

        return carrier.get(TRACE_PARENT);
    }

    public <T> T withRestoredTrace(String traceparentToRestore, String restoredSpanName, Supplier<T> action) {
        requireNonBlank(restoredSpanName, "restoredSpanName must not be blank");

        if (isBlank(traceparentToRestore)) {
            return action.get();
        }

        Tracer tracer = this.tracerProvider.getIfAvailable();
        Propagator propagator = this.propagatorProvider.getIfAvailable();

        if (tracer == null || propagator == null) {
            return action.get();
        }

        Map<String, String> carrier = new HashMap<>();
        carrier.put(TRACE_PARENT, traceparentToRestore);
        Span restoredSpan = propagator.extract(carrier, Map::get).name(restoredSpanName).start();

        try (Tracer.SpanInScope _ = tracer.withSpan(restoredSpan)) {
            return action.get();
        } catch (Exception e) {
            restoredSpan.error(e);
            throw e;
        } finally {
            restoredSpan.end();
        }
    }
}
