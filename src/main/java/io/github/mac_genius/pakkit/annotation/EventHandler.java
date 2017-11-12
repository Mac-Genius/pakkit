package io.github.mac_genius.pakkit.annotation;

import io.github.mac_genius.pakkit.event.EventPriority;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    EventPriority priority() default EventPriority.NORMAL;
}
