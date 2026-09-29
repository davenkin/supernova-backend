package com.company.andy.common.event.consume;

import com.company.andy.common.model.actor.PlatformActor;

import static com.company.andy.common.utils.CommonUtils.singleParameterizedArgumentClassOf;

// Base class for all event handlers, deals with handlers' priority, idempotency and transactionality

public abstract class AbstractEventHandler<T> {
    private final Class<?> eventClass;

    protected AbstractEventHandler() {
        this.eventClass = singleParameterizedArgumentClassOf(this.getClass());
    }

    public boolean isIdempotent() {
        // By default, all handlers are assumed to be not idempotent by themselves
        return false;
    }

    public boolean isTransactional() {
        // By default, all handlers are assumed to be transactional, we should make handlers to be transactional as much as possible
        return true;
    }

    public int priority() {
        // Smaller value means higher priority and will be handled first
        return 0;
    }

    public final String getName() {
        return this.getClass().getName();
    }

    public final boolean canHandle(Object event) {
        return this.eventClass.isAssignableFrom(event.getClass());
    }

//    public abstract void handle(T event);

    /// /    {
    /// /        PlatformActor actor = this.getActor(event);
    /// /        ActorMdcSupport.runWithMdc(actor, () -> this.handle(event, actor));
    /// /    }
//
//    protected abstract PlatformActor getActor(T event);
    public abstract void handle(T event, PlatformActor actor);
}
