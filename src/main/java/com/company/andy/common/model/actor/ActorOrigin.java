package com.company.andy.common.model.actor;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;

import static com.company.andy.common.model.actor.ActorOriginChannel.*;
import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

@NullMarked
public record ActorOrigin(ActorOriginChannel type, String originId) {

    public static ActorOrigin fromOrgApiCall(HttpServletRequest request) {
        requireNonNull(request, "request must not be null.");
        String httpMethod = request.getMethod();
        String path = request.getRequestURI();
        return new ActorOrigin(ORG_API, "%s[%s]".formatted(httpMethod, path));
    }

    public static ActorOrigin fromPlatformApiCall(HttpServletRequest request) {
        requireNonNull(request, "request must not be null.");
        String httpMethod = request.getMethod();
        String path = request.getRequestURI();
        return new ActorOrigin(PLATFORM_API, "%s[%s]".formatted(httpMethod, path));
    }

    public static ActorOrigin fromScheduledJob(String jobName) {
        requireNonBlank(jobName, "jobName must not be blank.");
        return new ActorOrigin(SCHEDULED_JOB, jobName);
    }

    public static ActorOrigin fromEvent(String eventClass, String eventId) {
        requireNonBlank(eventClass, "eventClass must not be blank.");
        requireNonBlank(eventId, "eventId must not be blank.");
        return new ActorOrigin(EVENT, "%s[%s]".formatted(eventClass, eventId));
    }

    public static ActorOrigin fromRobot(String name) {
        requireNonBlank(name, "name must not be blank.");
        return new ActorOrigin(ROBOT, name);
    }

    @Override
    public String toString() {
        return "%s:%s".formatted(type, originId);
    }
}
